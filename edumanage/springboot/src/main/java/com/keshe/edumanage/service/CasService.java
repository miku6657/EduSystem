package com.keshe.edumanage.service;


import com.keshe.edumanage.config.CasProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class CasService {


    private final CasProperties casProperties;


    private final RestTemplate restTemplate =
            new RestTemplate();



    /**
     * CAS验证ticket
     */
    public String validateTicket(String ticket){

        String url =
                casProperties.getServerUrlPrefix()
                        + "/serviceValidate"
                        + "?service="
                        + casProperties.getClientServiceUrl()
                        + "&ticket="
                        + ticket;


        String response =
                restTemplate.getForObject(
                        url,
                        String.class
                );


        // CAS认证成功
        if(response.contains("<cas:authenticationSuccess>")){


            int start =
                    response.indexOf("<cas:user>")
                            +
                            "<cas:user>".length();


            int end =
                    response.indexOf("</cas:user>");


            return response.substring(start,end);

        }


        return null;
    }

}
