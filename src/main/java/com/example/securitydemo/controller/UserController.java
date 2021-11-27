package com.example.securitydemo.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.securitydemo.dto.Role;
import com.example.securitydemo.dto.User;
import com.example.securitydemo.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController
{

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers()
    {
        List<User> users =  userService.getAllUser();
        if (users.isEmpty()) throw  new RuntimeException("cannot find any user ");
        return new ResponseEntity(users, HttpStatus.OK);
    }

    @PostMapping("/users/save")
    public ResponseEntity<User> saveUser(@RequestBody User user)
    {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(user.getName()).toUri();
        return ResponseEntity.created(location).body(userService.saveUser(user));
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role)
    {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(role.getName()).toUri();

        return ResponseEntity.created(location ).body(userService.saveRole(role));
    }

    @PostMapping("/role/addrole")
    public  ResponseEntity<?> addToUser(@RequestBody RoleToUser roleToUser )
    {
        userService.addRoleToUSer(roleToUser.getUsername(),roleToUser.getRole());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/token/refresh")
    public  void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

            //   log.info();
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            log.info("----something -----> {}" + authorizationHeader);

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))
            {
                try {

                    String refreshToken = authorizationHeader.substring("Bearer ".length());
                    Algorithm algorithm = Algorithm.HMAC256("securityCode".getBytes());
                    log.info("refreshToken--------------------->{}" + refreshToken);
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(refreshToken);
                    String username = decodedJWT.getSubject() ;
                    User user = userService.getUser(username);
                    String accessToken = JWT.create()
                            .withSubject(user.getUsername())
                            .withIssuer(request.getRequestURI())
                            .withExpiresAt(new Date(System.currentTimeMillis() + 2*60*1000))
                            .withClaim("roles" , user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                            .sign(algorithm);

                    Map<String ,String > tokens = new HashMap<>();

                    tokens.put("access_token" , accessToken);
                    tokens.put("refresh_token" , refreshToken);
                    response.setContentType(APPLICATION_JSON_VALUE );
                    new ObjectMapper().writeValue(response.getOutputStream(),tokens);


                }
                catch (Exception exception)
                {

                    response.setHeader("error",  exception.getMessage());
                    response.setStatus(FORBIDDEN.value());
                    Map<String,String> error = new HashMap<>();
                    log.error("Error logged  in {}",exception.getMessage());
                    error.put("error_message" , exception.getMessage());
                    response.setContentType(APPLICATION_JSON_VALUE );
                    response.setStatus(FORBIDDEN.value());

                    new ObjectMapper().writeValue(response.getOutputStream(),error);

                }
            }
        }
}

@Data
class RoleToUser{
    private String username ;
    private String role;
}
