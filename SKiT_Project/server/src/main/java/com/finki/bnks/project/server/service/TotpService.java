package com.finki.bnks.project.server.service;

import com.finki.bnks.project.server.security.custom_totp.CustomTotp;
import com.finki.bnks.project.server.security.custom_totp.Result;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class TotpService {
    public boolean isTotpValid(String secret, String code, int pastIntervals, int futureIntervals){
        CustomTotp totp = new CustomTotp(secret);
        return totp.verify(code, 2, 2).isValid();
    }

    public Result isCustomTotpValid(String secret, List<String> codes) {
        // check 25 hours into the past and future.
        long noOf30SecondsIntervals = TimeUnit.HOURS.toSeconds(25) / 30;
        CustomTotp totp = new CustomTotp(secret);
        String code1 = codes.get(0);
        String code2 = codes.get(1);
        String code3 = codes.get(2);
        return totp.verify(List.of(code1, code2, code3), noOf30SecondsIntervals, noOf30SecondsIntervals);
    }
}
