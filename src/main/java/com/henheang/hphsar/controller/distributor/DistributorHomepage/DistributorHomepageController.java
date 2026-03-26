package com.henheang.hphsar.controller.distributor.DistributorHomepage;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.service.DistributorHomepageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@Tag(name = "Distributor Homepage Controller")
@RequestMapping("${base.distributor.v1}/order_activities")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DistributorHomepageController extends BaseController {
    private final DistributorHomepageService distributorHomepageService;


    @GetMapping
    public ResponseEntity<?> getNewOrder() {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok("fetched successfully", distributorHomepageService.getNewOrder(currentUserId));
    }


    @GetMapping("/months")
    @Operation(summary = "Get total orders and products sold sort by months (YYYY-MM)")
    public ResponseEntity<?> getTotalByMonth(@RequestParam(defaultValue = "2023-01") String startDate, @RequestParam(defaultValue = "2023-05") String endDate) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();
        return ok("fetched successfully", distributorHomepageService.getTotalByMonth(currentUserId, startDate, endDate));
    }

}
