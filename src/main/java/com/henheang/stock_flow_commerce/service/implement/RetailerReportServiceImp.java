package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import com.henheang.stock_flow_commerce.model.retailer.report.CategoryNameAndTotalOfQty;
import com.henheang.stock_flow_commerce.model.retailer.report.RetailerReport;
import com.henheang.stock_flow_commerce.repository.RetailerReportRepository;
import com.henheang.stock_flow_commerce.service.RetailerReportService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RetailerReportServiceImp implements RetailerReportService {
    private final RetailerReportRepository retailerReportRepository;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
    SimpleDateFormat formatterForDatabase = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public RetailerReportServiceImp(RetailerReportRepository retailerReportRepository) {
        this.retailerReportRepository = retailerReportRepository;
    }

    @Override
    public RetailerReport getRetailerMonthlyReport(String startDate, String endDate) {

        AppUser appUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = appUser.getId();

        int totalOrder = 0;
        int totalAccepted = 0;
        int totalRejected = 0;
        int monthFromYear = 0;
        int totalExpense = 0;
        int distinctCount = 0;
        int totalRating = 0;
        double averageExpense = 0;
        double totalYearlyExpense = 0;
        int totalOrderInYear = 0;
        int purchaseOrderShopDifferentYear = 0;
        int totalExpenseDifferentYear = 0;
        int ratingDifferentYear = 0;
        double totalExpenseInDifferentYear = 0;
        int totalQuantityDifferentYear = 0;
        Integer totalQuantity = 0;
        List<String> categoryName = new ArrayList<>();
        List<String> categoryNameDifferentYear;
        List<Integer> totalOrderedShop=new ArrayList<>();
        List<Integer> totalPurchasedShop = new ArrayList<>();

        List<String> combinedList = new ArrayList<>();
        List<Integer> totalExpenseEachMonthFromRepo = new ArrayList<>();
        List<Integer> totalExpenseEachMonthFromRepoDifferentYear = new ArrayList<>();
        List<String> monthAndYearLabels = new ArrayList<>();
        List<Integer> totalRejectedAndAccepted = new ArrayList<>();
        List<Integer> totalQtyEachCategory = new ArrayList<>();
        List<CategoryNameAndTotalOfQty> categoryNameAndTotalOfQties = new ArrayList<>();
        YearMonth startYearMonth = parseYearMonth(startDate, "startDate");
        YearMonth endYearMonth = parseYearMonth(endDate, "endDate");
        int startYear = startYearMonth.getYear();
        int startMonth = startYearMonth.getMonthValue();
        int endYear = endYearMonth.getYear();
        int endMonth = endYearMonth.getMonthValue();
        RetailerReport retailerReport = new RetailerReport();
        LocalDate startDateValue = startYearMonth.atDay(1);
        LocalDate endDateValue = endYearMonth.atDay(1);

        if (startDateValue.isBefore(endDateValue) || startDateValue.equals(endDateValue)) {
            HashMap<Integer, String> monthMap = new HashMap<>();
            monthMap.put(1, "January");
            monthMap.put(2, "February");
            monthMap.put(3, "March");
            monthMap.put(4, "April");
            monthMap.put(5, "May");
            monthMap.put(6, "June");
            monthMap.put(7, "July");
            monthMap.put(8, "August");
            monthMap.put(9, "September");
            monthMap.put(10, "October");
            monthMap.put(11, "November");
            monthMap.put(12, "December");

            if (endMonth == startMonth && endYear == startYear) {

                totalAccepted = retailerReportRepository.getTotalRejectedAndAccepted(currentUserId, 5, startYear, startMonth);
                totalRejected = retailerReportRepository.getTotalRejectedAndAccepted(currentUserId, 6, endYear, endMonth);
                totalOrder = retailerReportRepository.getTotalMonthlyOrderByCurrentMonth(currentUserId, startYear, startMonth);

                totalQuantity = retailerReportRepository.getTotalQuantityOrder(currentUserId, 5, startYear, startMonth);
                if (totalQuantity == null) {
                    totalQuantity = 0;
                }
                categoryName = retailerReportRepository.getCategoryNameOrder(currentUserId, 5, startYear, startMonth);

                totalOrderedShop = retailerReportRepository.getPurchasedShopOrdered(currentUserId, 5, startYear, startMonth);
                Set<Integer> distinctElements = new HashSet<>(totalOrderedShop);
                distinctCount = distinctElements.size();


                Integer totalExpenseTest = retailerReportRepository.getTotalExpense(currentUserId, 5, startYear, startMonth);
                totalExpense = totalExpense + (totalExpenseTest == null ? 0 : totalExpenseTest);

                Integer totalRatingQty = retailerReportRepository.getTotalRatingStore(currentUserId, startYear, startMonth);
                totalRating = totalRating + (totalRatingQty == null ? 0 : totalRatingQty);

                averageExpense = totalExpense;

                Double totalYearlyExpenseMoney = retailerReportRepository.getTotalYearlyExpense(currentUserId, 5, startYear);
                totalYearlyExpense = totalYearlyExpense + (totalYearlyExpenseMoney == null ? 0 : totalYearlyExpenseMoney);

                Integer totalExpenseEachMonth = retailerReportRepository.getTotalExpense(currentUserId, 5, startYear, startMonth);
                totalExpenseEachMonthFromRepo.add(totalExpenseEachMonth == null ? 0 : totalExpenseEachMonth);

                String monthName = monthMap.get(startMonth);
                monthAndYearLabels.add(monthName);


                //For set data
                retailerReport.setTotalOrder(totalOrder);
                totalRejectedAndAccepted.add(totalAccepted);
                totalRejectedAndAccepted.add(totalRejected);
                retailerReport.setTotalRejectedAndAccepted(totalRejectedAndAccepted);
                retailerReport.setTotalQuantityOrder(totalQuantity);
                retailerReport.setCategoryNameOrdered(categoryName);
                retailerReport.setTotalPurchasedShop(distinctCount);
                retailerReport.setTotalExpenseOrdered(totalExpense);
                retailerReport.setTotalRatingShop(totalRating);
                retailerReport.setAverageMonthlyExpense(averageExpense);
                retailerReport.setTotalYearlyExpense(totalYearlyExpense);
                retailerReport.setTotalExpenseInEachMonth(totalExpenseEachMonthFromRepo);
                retailerReport.setMonthAndYearLabel(monthAndYearLabels);
                retailerReport.setTotalQtyEachCategory(retailerReportRepository.getTotalQtyInEachCategory(currentUserId, 5, startYear, startMonth));
                return retailerReport;
            }
            int nMonth = 1 + (endMonth - startMonth);
            Integer totalMonth = nMonth + monthFromYear;

            if (startMonth != endMonth && endYear == startYear) {
                double countMonth = 0;
                double formattedAverageExpense = 0;
                for (int i = startMonth; i <= endMonth; i++) {
                    countMonth++;

//                    Get total accepted and rejected
                    totalAccepted = totalAccepted + retailerReportRepository.getTotalRejectedAndAccepted(currentUserId, 5, startYear, i);
                    totalRejected = totalRejected + retailerReportRepository.getTotalRejectedAndAccepted(currentUserId, 6, startYear, i);
                    totalOrder = totalOrder + retailerReportRepository.getTotalMonthlyOrderByCurrentMonth(currentUserId, startYear, i);

                    //Get total quantity in each month
                    Integer totalQty = retailerReportRepository.getTotalQuantityOrder(currentUserId, 5, startYear, i);
                    totalQuantity = totalQuantity + (totalQty == null ? 0 : totalQty);

                    // Get name of category ordered
                    categoryName = retailerReportRepository.getCategoryNameOrder(currentUserId, 5, startYear, i);
                    combinedList.addAll(categoryName);

                totalOrderedShop.addAll(retailerReportRepository.getPurchasedShopOrdered(currentUserId, 5, startYear, i)) ;
                    Set<Integer> distinctElements = new HashSet<>(totalOrderedShop);
                    distinctCount = distinctElements.size();
                    totalPurchasedShop.add(distinctCount);
                    Integer totalExpenseDifferentMonth = retailerReportRepository.getTotalExpense(currentUserId, 5, startYear, i);
                    totalExpense = totalExpense + (totalExpenseDifferentMonth == null ? 0 : totalExpenseDifferentMonth);

                    totalRating = totalRating + retailerReportRepository.getTotalRatingStore(currentUserId, startYear, i);
                    averageExpense = totalExpense / countMonth;
                    DecimalFormat digitFormat = new DecimalFormat("#.##");
                    formattedAverageExpense = Double.parseDouble(digitFormat.format(averageExpense));

                    //Get expense total yearly
                    Double YearlyExpense = retailerReportRepository.getTotalYearlyExpense(currentUserId, 5, startYear);
                    totalYearlyExpense = YearlyExpense == null ? 0 : YearlyExpense;

                    //Get total expense in each year
                    Integer totalExpenseInDifferentMonth = retailerReportRepository.getTotalExpenseInDifferentYear(currentUserId, 5, startYear, i);
                    totalExpenseEachMonthFromRepo .add(totalExpenseInDifferentMonth == null ? 0 : totalExpenseInDifferentMonth);

                    // Get name of month that expensed
                    String monthName = monthMap.get(i);
                    monthAndYearLabels.add(monthName);
                    totalQtyEachCategory.addAll(retailerReportRepository.getTotalQtyInEachCategory(currentUserId, 5, startYear, i));
                }
                // Set data to model
                totalRejectedAndAccepted.add(totalAccepted);
                totalRejectedAndAccepted.add(totalRejected);
                retailerReport.setTotalRejectedAndAccepted(totalRejectedAndAccepted);
                retailerReport.setTotalOrder(totalOrder);
                retailerReport.setTotalQuantityOrder(totalQuantity);
                retailerReport.setTotalPurchasedShop(distinctCount);
                retailerReport.setCategoryNameOrdered(combinedList);
                retailerReport.setTotalExpenseOrdered(totalExpense);
                retailerReport.setTotalRatingShop(totalRating);
                retailerReport.setAverageMonthlyExpense(formattedAverageExpense);
                retailerReport.setTotalYearlyExpense(totalYearlyExpense);
                retailerReport.setTotalExpenseInEachMonth(totalExpenseEachMonthFromRepo);
                retailerReport.setMonthAndYearLabel(monthAndYearLabels);
                retailerReport.setTotalQtyEachCategory(totalQtyEachCategory);
                return retailerReport;
            }

            if (startYear != endYear) {

                int totalMonths = 0;
                LocalDate start = LocalDate.parse(startDate + "-01");
                LocalDate end = LocalDate.parse(endDate + "-01");

                // Start looping from the start date
                LocalDate current = start;
                List<String> categoryNameList = new ArrayList<>();
                List<Integer> totalItemEachCategory = new ArrayList<>();
                Map<String, Integer> categorySumMap = new HashMap<>();

                while (current.isBefore(end) || current.equals(end)) {

                    // Get the year and month from the current date
                    int year = current.getYear();
                    int month = current.getMonthValue();

                    totalOrderInYear = totalOrderInYear + retailerReportRepository.getTotalOrderFromDifferentYear(currentUserId, year, month);

                    totalRejected = totalRejected + retailerReportRepository.getTotalAcceptedAndRejectedFromDifferentYear(currentUserId, 6, year, month);

                    totalAccepted = totalAccepted + retailerReportRepository.getTotalAcceptedAndRejectedFromDifferentYear(currentUserId, 5, year, month);

                    Integer totalQtyDifferentYear = retailerReportRepository.getTotalQuantityInDifferenceYear(currentUserId, 5, year, month);
                    totalQuantityDifferentYear = totalQuantityDifferentYear + (totalQtyDifferentYear == null ? 0 : totalQtyDifferentYear);


                    categoryNameDifferentYear = retailerReportRepository.getCategoryNameOrderIndDifferentYear(currentUserId, 5, year, month);
                    combinedList.addAll(categoryNameDifferentYear);

                    purchaseOrderShopDifferentYear = purchaseOrderShopDifferentYear + retailerReportRepository.getTotalPurchasedShopDifferent(currentUserId, 5, year, month).size();

                    // Get total expense in different month and year
                    Integer expenseDifferentYear = retailerReportRepository.getTotalExpenseInDifferentYear(currentUserId, 5, year, month);
                    totalExpenseDifferentYear = totalExpenseDifferentYear + (expenseDifferentYear == null ? 0:expenseDifferentYear);

                    ratingDifferentYear = ratingDifferentYear + retailerReportRepository.getRatingInDifferentYear(currentUserId, 5, year, month);
                    //Get total expense in each month

                    Integer expenseEachMonthInDifferentYear = retailerReportRepository.getTotalExpenseInDifferentYear(currentUserId, 5, year, month);
                    totalExpenseEachMonthFromRepoDifferentYear .add(expenseEachMonthInDifferentYear == null ? 0 : expenseEachMonthInDifferentYear);

                    // Get name of month and year in different year
                    String monthName = monthMap.get(month);
                    String yearLabel = String.valueOf(year);
                    categoryNameAndTotalOfQties.addAll(retailerReportRepository.getCategoryNameAndTotalItem(currentUserId, 5, year, month));
                    categorySumMap = categoryNameAndTotalOfQties.stream()
                            .collect(Collectors.groupingBy(CategoryNameAndTotalOfQty::getCategoryName,
                                    Collectors.summingInt(CategoryNameAndTotalOfQty::getTotalItem)));
                    monthAndYearLabels.add(monthName + " " + yearLabel);
                    totalMonths++;

                    // Move to the next month
                    current = current.plusMonths(1);

                }
                categorySumMap.forEach((key, value) -> {
                    categoryNameList.add(key);
                    totalItemEachCategory.add(value);
                });

                for (int i = startYear; i <= endYear; i++) {
                    Double YearlyExpenseInDifferentYear = retailerReportRepository.getTotalYearlyInDifferentYear(currentUserId, 5, i);
                    totalExpenseInDifferentYear = totalExpenseInDifferentYear + (YearlyExpenseInDifferentYear == null ? 0: YearlyExpenseInDifferentYear);
                }

                Double averageExpenseInDifferentYear = (totalExpenseDifferentYear / (double) totalMonths);
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                double formattedAverageExpense = Double.parseDouble(decimalFormat.format(averageExpenseInDifferentYear));
                retailerReport.setAverageMonthlyExpense(formattedAverageExpense);
                totalRejectedAndAccepted.add(totalAccepted);
                totalRejectedAndAccepted.add(totalRejected);
                retailerReport.setTotalRejectedAndAccepted(totalRejectedAndAccepted);
                retailerReport.setTotalOrder(totalOrderInYear);
                retailerReport.setTotalQuantityOrder(totalQuantityDifferentYear);
                retailerReport.setCategoryNameOrdered(categoryNameList);
                retailerReport.setTotalQtyEachCategory(totalItemEachCategory);
                retailerReport.setTotalPurchasedShop(purchaseOrderShopDifferentYear);
                retailerReport.setTotalExpenseOrdered(totalExpenseDifferentYear);
                retailerReport.setTotalRatingShop(ratingDifferentYear);
                retailerReport.setTotalYearlyExpense(totalExpenseInDifferentYear);
                retailerReport.setTotalExpenseInEachMonth(totalExpenseEachMonthFromRepoDifferentYear);
                retailerReport.setMonthAndYearLabel(monthAndYearLabels);
            }
            return retailerReport;
        } else {
            throw new BadRequestException("Invalid date range: End date must be after start date");
        }
    }

    private YearMonth parseYearMonth(String date, String fieldName) {
        try {
            return YearMonth.parse(date, YEAR_MONTH_FORMATTER);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new BadRequestException("Invalid " + fieldName + ". Format should be yyyy-MM");
        }
    }
}


    
