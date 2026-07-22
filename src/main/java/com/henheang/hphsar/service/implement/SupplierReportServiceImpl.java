package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.InternalServerErrorException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.report.SupplierReport;
import com.henheang.hphsar.repository.SupplierReportRepository;
import com.henheang.hphsar.repository.StoreRepository;
import com.henheang.hphsar.service.SupplierReportService;
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
public class SupplierReportServiceImpl implements SupplierReportService {
    SimpleDateFormat monthFormatter = new SimpleDateFormat("MM");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);
    private final SupplierReportRepository supplierReportRepository;
    private final StoreRepository storeRepository;

    public SupplierReportServiceImpl(SupplierReportRepository supplierReportRepository, StoreRepository storeRepository) {
        this.supplierReportRepository = supplierReportRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public SupplierReport getSupplierReport(String startDate, String endDate) throws ParseException {
        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer storeId = storeRepository.getStoreIdByUserId(appUser.getId());

        LocalDate startDateLocal = parseDate(startDate, "start");
        LocalDate endDateLocal = parseDate(endDate, "end");

        if (endDateLocal.isBefore(startDateLocal)) {
            throw new BadRequestException("End date need to be higher than or equal start date.");
        }

        String startDateStr = startDateLocal.withDayOfMonth(1).toString();
        String endDateStr = endDateLocal.withDayOfMonth(endDateLocal.lengthOfMonth()).toString();

        SupplierReport supplierReport = new SupplierReport();
        supplierReport.setTotalExpense(supplierReportRepository.getTotalExpense(startDateStr, endDateStr, storeId));
        if (supplierReport.getTotalExpense() == null) {
            throw new InternalServerErrorException("Fail to get total expense.");
        }
        supplierReport.setTotalProfit(supplierReportRepository.getTotalProfit(startDateStr, endDateStr, storeId));
        if (supplierReport.getTotalProfit() == null) {
            throw new InternalServerErrorException("Fail to get total sale.");
        }
        supplierReport.setTotalOrder(supplierReportRepository.getOrder(startDateStr, endDateStr, storeId));
        if (supplierReport.getTotalOrder() == null) {
            throw new InternalServerErrorException("Fail to get total order.");
        }

        Period period = Period.between(startDateLocal.withDayOfMonth(1), endDateLocal.withDayOfMonth(1));

        if (period.getYears() >= 2) {
            List<String> years = new ArrayList<>();
            LocalDate current = startDateLocal.withDayOfMonth(1);
            while (!current.isAfter(endDateLocal)) {
                years.add(String.valueOf(current.getYear()));
                current = current.plusYears(1);
            }
            // Remove duplicates if any
            List<String> uniqueYears = new ArrayList<>(new java.util.LinkedHashSet<>(years));
            supplierReport.setPeriod(uniqueYears);
            supplierReport.setPeriodName(uniqueYears);
            List<Integer> orderOfYear = new ArrayList<>();
            for (String year : uniqueYears) {
                String yearStart = year + "-01-01";
                String yearEnd = year + "-12-31";
                Integer orders = supplierReportRepository.getOrderOfMonth(storeId, yearStart, yearEnd);
                orderOfYear.add(orders == null ? 0 : orders);
            }
            supplierReport.setOrderPerMonth(orderOfYear);
        } else {
            List<String> months = getMonthsBetweenDates(startDateLocal.withDayOfMonth(1), endDateLocal.withDayOfMonth(1).plusMonths(1));
            supplierReport.setPeriodName(months);
            List<String> intMonth = new ArrayList<>();
            for (String name : months) {
                intMonth.add(String.valueOf(Month.valueOf(name.toUpperCase()).getValue()));
            }
            supplierReport.setPeriod(intMonth);
            List<Integer> orderOfMonth = new ArrayList<>();
            LocalDate current = startDateLocal.withDayOfMonth(1);
            while (!current.isAfter(endDateLocal.withDayOfMonth(1))) {
                String monthStart = current.toString();
                String monthEnd = current.withDayOfMonth(current.lengthOfMonth()).toString();
                Integer orders = supplierReportRepository.getOrderOfMonth(storeId, monthStart, monthEnd);
                orderOfMonth.add(orders == null ? 0 : orders);
                current = current.plusMonths(1);
            }
            supplierReport.setOrderPerMonth(orderOfMonth);
        }
        return supplierReport;
    }

    private LocalDate parseDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.isBlank() || dateStr.equals("yyyy-mm")) {
            throw new BadRequestException("Invalid " + fieldName + " date. Format should be yyyy-MM or yyyy-MM-dd");
        }
        try {
            if (dateStr.matches("\\d{4}-\\d{2}")) {
                return LocalDate.parse(dateStr + "-01");
            } else if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(dateStr);
            }
        } catch (Exception e) {
            // fall through to exception
        }
        throw new BadRequestException("Invalid " + fieldName + " date format. Expected yyyy-MM or yyyy-MM-dd");
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
