package com.MailModule.Interceptor;

import com.MailModule.DTO.EmployeeConstructorDTO;
import com.MailModule.Mapper.EmployeeConstructorMapper;
import com.MailModule.Repository.MailRepository;
import com.MailModule.Wrapper.CachedBodyHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class EmployeeInterceptor extends OncePerRequestFilter {

    @Autowired
    MailRepository mailRepository;

    @Autowired
    EmployeeConstructorMapper employeeConstructorMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("requestURI" + requestURI);

        if (!requestURI.contains("Employee")) {
            filterChain.doFilter(request, response);
            return;
        }

        switch (request.getMethod()) {
            case "POST", "PUT" -> handlePostAndPutRequest(request, response, filterChain);

            case "GET" -> handleGetRequest(request, response, filterChain);

            default -> filterChain.doFilter(request, response);
        }
    }

    public void handleGetRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, resp);

        if (resp.getStatus() != 200) {
            resp.copyBodyToResponse();
            return;
        }

        byte[] responseBody = resp.getContentAsByteArray();
        Charset charset = resolveCharset(
                resp.getCharacterEncoding()
        );
        String res = new String(responseBody, StandardCharsets.UTF_8);
        List<JSONObject> jsonObjectList = convertDataToJsonObject(res);
        List<EmployeeConstructorDTO> employeeConstructorDTOList = employeeConstructorMapper.convertListToEmployeeDTO(jsonObjectList);


        String originalJson = new String(
                responseBody,
                charset
        );

        String modifiedJson;
        ObjectMapper objectMapper = new ObjectMapper();

        if (originalJson.trim().startsWith("[")) {
            modifiedJson = objectMapper.writeValueAsString(
                    employeeConstructorDTOList
            );
            log.info("list: " + modifiedJson);
        } else {
            modifiedJson = employeeConstructorDTOList.isEmpty()
                    ? "{}"
                    : objectMapper.writeValueAsString(
                    employeeConstructorDTOList.getFirst()
            );
            log.info("list: " + modifiedJson);
        }
        byte[] modifiedBody = modifiedJson.getBytes(charset);
        resp.resetBuffer();
        resp.setContentType("application/json");
        resp.setHeader("Transfer-Encoding", "");
        resp.setCharacterEncoding(charset.name());

        resp.getOutputStream().write(modifiedBody);

        resp.copyBodyToResponse();
    }

    public void handlePostAndPutRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException {

        CachedBodyHttpServletRequest wrappedRequest =
                new CachedBodyHttpServletRequest(request);

        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        String payload = new String(
                wrappedRequest.getCachedBody(),
                StandardCharsets.UTF_8
        );

        List<JSONObject> jsonObjectList =
                convertDataToJsonObject(payload);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            if (wrappedResponse.getStatus() == HttpServletResponse.SC_OK) {
                insertEmail(jsonObjectList);
            }

        } catch (DataIntegrityViolationException e) {
            wrappedResponse.resetBuffer();
            wrappedResponse.setStatus(HttpServletResponse.SC_CONFLICT);
            wrappedResponse.setContentType("text/plain");
            wrappedResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

            wrappedResponse.getWriter().write(
                    "Email già presente per un altro utente"
            );
        } finally {
            wrappedResponse.copyBodyToResponse();
        }
    }

    public List<JSONObject> convertDataToJsonObject(String data) {
        List<JSONObject> jsonObjectList = new ArrayList<>();

        try {
            if (data.startsWith("[") && data.endsWith("]")) {
                JSONArray jsonArray = new JSONArray(data);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);

                    if (jsonObject == null) {
                        continue;
                    }

                    jsonObjectList.add(jsonObject);
                }
            } else {
                JSONObject jsonObject = new JSONObject(data);
                jsonObjectList.add(jsonObject);
            }

        } catch (JSONException exception) {
            log.info("Impossibile leggere il payload Employee", exception);
        }

        return jsonObjectList;
    }

    public boolean isPutOrPostRequest(HttpServletRequest request) {
        return request.getMethod().equals("PUT") || request.getMethod().equals("POST");
    }

    private Charset resolveCharset(String encoding) {
        try {
            return Charset.forName(encoding);
        } catch (Exception exception) {
            return StandardCharsets.UTF_8;
        }
    }

    //@Transactional
    public void insertEmail(List<JSONObject> jsonObjectList) {
        for (JSONObject jsonObject : jsonObjectList) {
            String email = null;
            String employeeId = null;
            if (jsonObject.has("Email") && !jsonObject.isNull("Email")) {
                email = jsonObject.optString("Email", null);
            }

            if (jsonObject.has("EmployeeId") && !jsonObject.isNull("EmployeeId")) {
                employeeId = jsonObject.optString("EmployeeId", null);
            } else {
                continue;
            }

            try {
                mailRepository.setupEmail(email, employeeId);
                mailRepository.flush();
            } catch (DataIntegrityViolationException e) {
                throw new DataIntegrityViolationException("Email già presente per un altro utente");
            }
        }
    }
}
