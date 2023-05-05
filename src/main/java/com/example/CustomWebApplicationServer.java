package com.example;

import com.example.calculator.domain.Calculator;
import com.example.calculator.domain.PositiveNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomWebApplicationServer {
    private final int port;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 로그를 남겨주기 위해서 선언
    private static final Logger logger = LoggerFactory.getLogger(CustomWebApplicationServer.class);

    public CustomWebApplicationServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        // 지정한 port로 서버 소켓을 만든 이후에
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("[com.example.CustomWebApplicationServer] started {} port.", port);

            Socket clientSocket;
            logger.info("[com.example.CustomWebApplicationServer] waiting for client.");

            // 서버 소켓이 client를 기다리게 한다.
            while ((clientSocket = serverSocket.accept()) != null) {
                logger.info("[com.example.CustomWebApplicationServer] client connected!");

                /**
                 * Step1 - 사용자 요청을 main thread가 처리하도록 한다.
                 * 이 부분을 통해서 http protocal이 어떻게 생겼는지 확인 가능
                 *
                 * 01:01:28.455 [main] INFO com.example.CustomWebApplicationServer - GET / HTTP/1.1
                 * 01:01:28.456 [main] INFO com.example.CustomWebApplicationServer - Host: localhost:8080
                 * 01:01:28.456 [main] INFO com.example.CustomWebApplicationServer - Connection: Keep-Alive
                 * 01:01:28.456 [main] INFO com.example.CustomWebApplicationServer - User-Agent: Apache-HttpClient/4.5.13 (Java/11.0.14.1)
                 * 01:01:28.456 [main] INFO com.example.CustomWebApplicationServer - Accept-Encoding: gzip,deflate
                 *
                 * 즉, 내부적으로 어떤 요청인지 판단 후 spring에 request를 보내는 tomcat, nginx 역할 확인
                 */
                // try-with-resources AutoCloseable로 인해서 자원 반납 finally가 필요없음
                try (InputStream in = clientSocket.getInputStream(); OutputStream out = clientSocket.getOutputStream()){
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    DataOutputStream dos = new DataOutputStream(out);

                    HttpRequest httpRequest = new HttpRequest(br);

                    if(httpRequest.isGetRequest() && httpRequest.matchPath("/calculate")){
                        QueryStrings queryStrings = httpRequest.getQueryStrings();

                        int operand1 = Integer.parseInt(queryStrings.getValue("operand1"));
                        String operator = queryStrings.getValue("operator");
                        int operand2 = Integer.parseInt(queryStrings.getValue("operand2"));

                        int result = Calculator.calculate(new PositiveNumber(operand1), operator, new PositiveNumber(operand2));
                        byte[] body = String.valueOf(result).getBytes(StandardCharsets.UTF_8);

                        HttpResponse response = new HttpResponse(dos);
                        response.response200Header("application/json", body.length);
                        response.responseBody(body);

                    }
                   /* String line;
                    while ((line = br.readLine()) != "") {
                        logger.info(line);
                    }*/
                }

                /**
                 * Step3 - Thread Pool을 적용해 안정적인 서비스가 가능하도록 한다.
                 */
                //executorService.execute(new ClientRequestHandler(clientSocket));
            }
        }
    }
}
