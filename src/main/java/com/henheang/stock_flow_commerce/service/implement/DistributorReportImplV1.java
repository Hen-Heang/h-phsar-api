package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.exception.InternalServerErrorException;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.report.DistributorReport;
import com.henheang.stock_flow_commerce.repository.DistributorReportRepository;
import com.henheang.stock_flow_commerce.repository.StoreRepository;
import com.henheang.stock_flow_commerce.service.DistributorReportService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class DistributorReportImplV1 implements DistributorReportService {
    SimpleDateFormat monthFormatter = new SimpleDateFormat("MM");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);
    private final DistributorReportRepository distributorReportRepository;
    private final StoreRepository storeRepository;

    public DistributorReportImplV1(DistributorReportRepository distributorReportRepository, StoreRepository storeRepository) {
        this.distributorReportRepository = distributorReportRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public DistributorReport  getDistributorReport(String startDate, String endDate) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer storeId = storeRepository.getStoreIdByUserId(appUser.getId());
        if (!isValidDate(startDate)) {
            throw new BadRequestException("Invalid start date. Format should be yyyy-mm");
        }
        if (!isValidDate(endDate)) {
            throw new BadRequestException("Invalid end date. Format should be yyyy-mm");
        }
        Integer startYear = Integer.parseInt(startDate.substring(0, 4));
        Integer startMonth = Integer.parseInt(startDate.substring(5));
        LocalDate startDateLocal = LocalDate.of(startYear, startMonth, 1);
        if (startMonth > 12 || startMonth < 1) {
            throw new BadRequestException("Month is invalid. There is only 12 month in a year.");
        }
        Integer endYear = Integer.parseInt(endDate.substring(0, 4));
        Integer endMonth = Integer.parseInt(endDate.substring(5));
        LocalDate endDateLocal = LocalDate.of(endYear, endMonth, 1);
        if (endMonth > 12 || endMonth < 1) {
            throw new BadRequestException("Month is invalid. There is only 12 month in a year.");
        }
        Period period = Period.between(startDateLocal, endDateLocal);
        // 1st check allow endDate have to bigger than startDate
        if (endYear - startYear < 0 || endMonth - startMonth < 0) {
            throw new BadRequestException("End date need to be higher than or equal start date.");
        }
        startDate = startDate + "-01";
        String variousDate;
        if (endMonth == 2){
            endDate = endDate + "-28";
            variousDate = "-28";
        } else if (endMonth % 2 == 0 && endMonth < 8) {
            endDate = endDate + "-30";
            variousDate = "-30";
        } else if (endMonth % 2 == 0){
            endDate = endDate + "-31";
            variousDate = "-31";
        } else if (endMonth < 8) {
            endDate = endDate + "-31";
            variousDate = "-31";
        } else {
            endDate = endDate + "-30";
            variousDate = "-30";
        }

        DistributorReport distributorReport = new DistributorReport();
        distributorReport.setTotalExpense(distributorReportRepository.getTotalExpense(startDate, endDate, storeId));
        if (distributorReport.getTotalExpense() == null) {
            throw new InternalServerErrorException("Fail to get total expense.");
        }
        distributorReport.setTotalProfit(distributorReportRepository.getTotalProfit(startDate, endDate, storeId));
        if (distributorReport.getTotalProfit() == null) {
            throw new InternalServerErrorException("Fail to get total sale.");
        }
        distributorReport.setTotalOrder(distributorReportRepository.getOrder(startDate, endDate, storeId));
        if (distributorReport.getTotalOrder() == null) {
            throw new InternalServerErrorException("Fail to get total order.");
        }

        ///////////////////////////////////

        if (period.getYears() >= 2){
            Integer totalYears = endYear - startYear + 1;
            List<String> year = new ArrayList<>();
            Integer beginning = startYear;
            for (int i = 0; i < totalYears; i++){
                year.add(beginning.toString());
                beginning++;
            }
            distributorReport.setPeriod(year);
            distributorReport.setPeriodName(year);
            List<Integer> orderOfYear = new ArrayList<>();
            Integer beginning2 = startYear;
            for (int i = 0; i < totalYears; i++){
                System.out.println(beginning2 + "-" + "01" + "-" + "01 "  + beginning2 + "-" + "12" + "-31");
                orderOfYear.add(distributorReportRepository.getOrderOfMonth(storeId, beginning2 + "-" + "01" + "-" + "01", beginning2 + "-" + "12" + "-31") == null ? 0 : distributorReportRepository.getOrderOfMonth(storeId, beginning2 + "-" + "01" + "-" + "01", beginning2 + "-" + "12" + "-31"));
                beginning2++;
            }
            distributorReport.setOrderPerMonth(orderOfYear);

        }
        //////////////////////////////////
        else
        {
            List<String> months = getMonthsBetweenDates(startDateLocal, endDateLocal.plusMonths(1));
            distributorReport.setPeriodName(months);
            List<String> intMonth = new ArrayList<>();
            for (String name : distributorReport.getPeriodName()) {
                int monthNum = convertMonthToIntegerMonth(name);
                intMonth.add(String.valueOf(monthNum));
            }
            distributorReport.setPeriod(intMonth);
            List<Integer> orderOfMonth = new ArrayList<>();
            Integer startYearLoop = startYear;
            for (String month : distributorReport.getPeriod()) {
                if (Objects.equals(month, "12")) {
                    startYearLoop++;
                }
                orderOfMonth.add(distributorReportRepository.getOrderOfMonth(storeId, startYearLoop + "-" + month + "-" + "01", startYearLoop + "-" + month + "-28") == null ? 0 : distributorReportRepository.getOrderOfMonth(storeId, startYearLoop + "-" + month + "-" + "01", startYearLoop + "-" + month + "-28"));
            }
            distributorReport.setOrderPerMonth(orderOfMonth);
        }
        return distributorReport;
    }

















    public static List<String> getYearsBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<String> years = new ArrayList<>();

        while (startDate.isBefore(endDate)) {
            years.add(String.valueOf(startDate.getYear()));
            startDate = startDate.plusYears(1);
        }

        return years;
    }




    public static boolean isValidDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        try {
            format.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static List<String> getMonthsBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<Month> months = new ArrayList<>();

        while (startDate.isBefore(endDate)) {
            months.add(startDate.getMonth());
            startDate = startDate.plusMonths(1);
        }
        List<String> stringMonth = new ArrayList<>();
        for (Month month : months) {
            stringMonth.add(month.toString());
        }
        return stringMonth;
    }

    public static int convertMonthToIntegerMonth(String monthName) {
        Month month = Month.valueOf(monthName);
        return month.getValue();
    }
}
