package com.henheang.stock_flow_commerce.controller.otp;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.ApiResponse;
import com.henheang.stock_flow_commerce.service.OtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Generate OTP")
@RequestMapping("/authorization/api/v1/otp")
public class OTPController {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;
    private final OtpService otpService;

    public OTPController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@RequestParam String email){
        String otpResponse = otpService.generateOtp(email);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.CREATED.value())
                .message("New OTP generated.")
                .data(otpResponse)
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> activateAccount(@RequestParam Integer otp, @RequestParam String email){
        if (otp > 2147483646){
            throw new BadRequestException("Integer value can not exceed 2147483646");
        }
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Activated successfully")
                .data(otpService.verifyOtp(otp, email))
                .date(formatter.format(date = new Date()))
                .build();
        return ResponseEntity.ok(response);
    }
}
