package gob.yucatan.sicasy.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    private final ServletContext context;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorPath = "/login?error=";

        if(exception instanceof BadCredentialsException){
            errorPath += exception.getMessage();
        } else {
            errorPath += "true";
        }

        RequestDispatcher dispatcher = context.getRequestDispatcher(errorPath);
        dispatcher.forward(request, response);
    }
}
