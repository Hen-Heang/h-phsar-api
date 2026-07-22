package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.buyer.report.CategoryNameAndTotalOfQty;
import com.henheang.hphsar.model.buyer.report.BuyerReport;
import com.henheang.hphsar.repository.BuyerReportRepository;
import com.henheang.hphsar.service.BuyerReportService;
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
public class BuyerReportServiceImpl implements BuyerReportService {
    private final BuyerReportRepository buyerReportRepository;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
    SimpleDateFormat formatterForDatabase = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public BuyerReportServiceImpl(BuyerReportRepository buyerReportRepository) {
        this.buyerReportRepository = buyerReportRepository;
    }

    @Override
    public BuyerReport getBuyerMonthlyReport(String startDate, String endDate) {

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
        BuyerReport buyerReport = new BuyerReport();
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

                totalAccepted = buyerReportRepository.getTotalRejectedAndAccepted(currentUserId, 5, startYear, startMonth);
                totalRejected = buyerReportRepository.getTotalRejectedAndAccepted(currentUserId, 6, endYear, endMonth);
                totalOrder = buyerReportRepository.getTotalMonthlyOrderByCurrentMonth(currentUserId, startYear, startMonth);

                totalQuantity = buyerReportRepository.getTotalQuantityOrder(currentUserId, 5, startYear, startMonth);
                if (totalQuantity == null) {
                    totalQuantity = 0;
                }
                categoryName = buyerReportRepository.getCategoryNameOrder(currentUserId, 5, startYear, startMonth);

                totalOrderedShop = buyerReportRepository.getPurchasedShopOrdered(currentUserId, 5, startYear, startMonth);
                Set<Integer> distinctElements = new HashSet<>(totalOrderedShop);
                distinctCount = distinctElements.size();


                Integer totalExpenseTest = buyerReportRepository.getTotalExpense(currentUserId, 5, startYear, startMonth);
                totalExpense = totalExpense + (totalExpenseTest == null ? 0 : totalExpenseTest);

                Integer totalRatingQty = buyerReportRepository.getTotalRatingStore(currentUserId, startYear, startMonth);
                totalRating = totalRating + (totalRatingQty == null ? 0 : totalRatingQty);

                averageExpense = totalExpense;

                Double totalYearlyExpenseMoney = buyerReportRepository.getTotalYearlyExpense(currentUserId, 5, startYear);
                totalYearlyExpense = totalYearlyExpense + (totalYearlyExpenseMoney == null ? 0 : totalYearlyExpenseMoney);

                Integer totalExpenseEachMonth = buyerReportRepository.getTotalExpense(currentUserId, 5, startYear, startMonth);
                totalExpenseEachMonthFromRepo.add(totalExpenseEachMonth == null ? 0 : totalExpenseEachMonth);

                String monthName = monthMap.get(startMonth);
                monthAndYearLabels.add(monthName);


                //For set data
                buyerReport.setTotalOrder(totalOrder);
                totalRejectedAndAccepted.add(totalAccepted);
                totalRejectedAndAccepted.add(totalRejected);
                buyerReport.setTotalRejectedAndAccepted(totalRejectedAndAccepted);
                buyerReport.setTotalQuantityOrder(totalQuantity);
                buyerReport.setCategoryNameOrdered(categoryName);
                buyerReport.setTotalPurchasedShop(distinctCount);
                buyerReport.setTotalExpenseOrdered(totalExpense);
                buyerReport.setTotalRatingShop(totalRating);
                buyerReport.setAverageMonthlyExpense(averageExpense);
                buyerReport.setTotalYearlyExpense(totalYearlyExpense);
                buyerReport.setTotalExpenseInEachMonth(totalExpenseEachMonthFromRepo);
                buyerReport.setMonthAndYearLabel(monthAndYearLabels);
                buyerReport.setTotalQtyEachCategory(buyerReportRepository.getTotalQtyInEachCategory(currentUserId, 5, startYear, startMonth));
                return buyerReport;
            }
            int nMonth = 1 + (endMonth - startMonth);
            Integer totalMonth = nMonth + monthFromYear;

            if (startMonth != endMonth && endYear == startYear) {
                double countMonth = 0;
                double formattedAverageExpense = 0;
                for (int i = startMonth; i <= endMonth; i++) {
                    countMonth++;

//                    Get total accepted and rejected
                    totalAccepted = totalAccepted + buyerReportRepository.getTotalRejectedAndAccepted(currentUserId, 5, startYear, i);
                    totalRejected = totalRejected + buyerReportRepository.getTotalRejectedAndAccepted(currentUserId, 6, startYear, i);
                    totalOrder = totalOrder + buyerReportRepository.getTotalMonthlyOrderByCurrentMonth(currentUserId, startYear, i);

                    //Get total quantity in each month
                    Integer totalQty = buyerReportRepository.getTotalQuantityOrder(currentUserId, 5, startYear, i);
                    totalQuantity = totalQuantity + (totalQty == null ? 0 : totalQty);

                    // Get name of category ordered
                    categoryName = buyerReportRepository.getCategoryNameOrder(currentUserId, 5, startYear, i);
                    combinedList.addAll(categoryName);

                totalOrderedShop.addAll(buyerReportRepository.getPurchasedShopOrdered(currentUserId, 5, startYear, i)) ;
                    Set<Integer> distinctElements = new HashSet<>(totalOrderedShop);
                    distinctCount = distinctElements.size();
                    totalPurchasedShop.add(distinctCount);
                    Integer totalExpenseDifferentMonth = buyerReportRepository.getTotalExpense(currentUserId, 5, startYear, i);
                    totalExpense = totalExpense + (totalExpenseDifferentMonth == null ? 0 : totalExpenseDifferentMonth);

                    totalRating = totalRating + buyerReportRepository.getTotalRatingStore(currentUserId, startYear, i);
                    averageExpense = totalExpense / countMonth;
                    DecimalFormat digitFormat = new DecimalFormat("#.##");
                    formattedAverageExpense = Double.parseDouble(digitFormat.format(averageExpense));

                    //Get expense total yearly
                    Double YearlyExpense = buyerReportRepository.getTotalYearlyExpense(currentUserId, 5, startYear);
                    totalYearlyExpense = YearlyExpense == null ? 0 : YearlyExpense;

                    //Get total expense in each year
                    Integer totalExpenseInDifferentMonth = buyerReportRepository.getTotalExpenseInDifferentYear(currentUserId, 5, startYear, i);
                    totalExpenseEachMonthFromRepo .add(totalExpenseInDifferentMonth == null ? 0 : totalExpenseInDifferentMonth);

                    // Get name of month that expensed
                    String monthName = monthMap.get(i);
                    monthAndYearLabels.add(monthName);
                    totalQtyEachCategory.addAll(buyerReportRepository.getTotalQtyInEachCategory(currentUserId, 5, startYear, i));
                }
                // Set data to model
                totalRejectedAndAccepted.add(totalAccepted);
                totalRejectedAndAccepted.add(totalRejected);
                buyerReport.setTotalRejectedAndAccepted(totalRejectedAndAccepted);
                buyerReport.setTotalOrder(totalOrder);
                buyerReport.setTotalQuantityOrder(totalQuantity);
                buyerReport.setTotalPurchasedShop(distinctCount);
                buyerReport.setCategoryNameOrdered(combinedList);
                buyerReport.setTotalExpenseOrdered(totalExpense);
                buyerReport.setTotalRatingShop(totalRating);
                buyerReport.setAverageMonthlyExpense(formattedAverageExpense);
                buyerReport.setTotalYearlyExpense(totalYearlyExpense);
                buyerReport.setTotalExpenseInEachMonth(totalExpenseEachMonthFromRepo);
                buyerReport.setMonthAndYearLabel(monthAndYearLabels);
                buyerReport.setTotalQtyEachCategory(totalQtyEachCategory);
                return buyerReport;
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

                    totalOrderInYear = totalOrderInYear + buyerReportRepository.getTotalOrderFromDifferentYear(currentUserId, year, month);

                    totalRejected = totalRejected + buyerReportRepository.getTotalAcceptedAndRejectedFromDifferentYear(currentUserId, 6, year, month);

                    totalAccepted = totalAccepted + buyerReportRepository.getTotalAcceptedAndRejectedFromDifferentYear(currentUserId, 5, year, month);

                    Integer totalQtyDifferentYear = buyerReportRepository.getTotalQuantityInDifferenceYear(currentUserId, 5, year, month);
                    totalQuantityDifferentYear = totalQuantityDifferentYear + (totalQtyDifferentYear == null ? 0 : totalQtyDifferentYear);


                    categoryNameDifferentYear = buyerReportRepository.getCategoryNameOrderIndDifferentYear(currentUserId, 5, year, month);
                    combinedList.addAll(categoryNameDifferentYear);

                    purchaseOrderShopDifferentYear = purchaseOrderShopDifferentYear + buyerReportRepository.getTotalPurchasedShopDifferent(currentUserId, 5, year, month).size();

                    // Get total expense in different month and year
                    Integer expenseDifferentYear = buyerReportRepository.getTotalExpenseInDifferentYear(currentUserId, 5, year, month);
                    totalExpenseDifferentYear = totalExpenseDifferentYear + (expenseDifferentYear == null ? 0:expenseDifferentYear);

                    ratingDifferentYear = ratingDifferentYear + buyerReportRepository.getRatingInDifferentYear(currentUserId, 5, year, month);
                    //Get total expense in each month

                    Integer expenseEachMonthInDifferentYear = buyerReportRepository.getTotalExpenseInDifferentYear(currentUserId, 5, year, month);
                    totalExpenseEachMonthFromRepoDifferentYear .add(expenseEachMonthInDifferentYear == null ? 0 : expenseEachMonthInDifferentYear);

                    // Get name of month and year in different year
                    String monthName = monthMap.get(month);
                    String yearLabel = String.valueOf(year);
                    categoryNameAndTotalOfQties.addAll(buyerReportRepository.getCategoryNameAndTotalItem(currentUserId, 5, year, month));
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
                    Double YearlyExpenseInDifferentYear = buyerReportRepository.getTotalYearlyInDifferentYear(currentUserId, 5, i);
                    totalExpenseInDifferentYear = totalExpenseInDifferentYear + (YearlyExpenseInDifferentYear == null ? 0: YearlyExpenseInDifferentYear);
                }

                Double averageExpenseInDifferentYear = (totalExpenseDifferentYear / (double) totalMonths);
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                double formattedAverageExpense = Double.parseDouble(decimalFormat.format(averageExpenseInDifferentYear));
                buyerReport.setAverageMonthlyExpense(formattedAverageExpense);
                totalRejectedAndAccepted.add(totalAccepted);
                totalRejectedAndAccepted.add(totalRejected);
                buyerReport.setTotalRejectedAndAccepted(totalRejectedAndAccepted);
                buyerReport.setTotalOrder(totalOrderInYear);
                buyerReport.setTotalQuantityOrder(totalQuantityDifferentYear);
                buyerReport.setCategoryNameOrdered(categoryNameList);
                buyerReport.setTotalQtyEachCategory(totalItemEachCategory);
                buyerReport.setTotalPurchasedShop(purchaseOrderShopDifferentYear);
                buyerReport.setTotalExpenseOrdered(totalExpenseDifferentYear);
                buyerReport.setTotalRatingShop(ratingDifferentYear);
                buyerReport.setTotalYearlyExpense(totalExpenseInDifferentYear);
                buyerReport.setTotalExpenseInEachMonth(totalExpenseEachMonthFromRepoDifferentYear);
                buyerReport.setMonthAndYearLabel(monthAndYearLabels);
            }
            return buyerReport;
        } else {
            throw new BadRequestException("Invalid date range: End date must be after start date");
        }
    }

    private YearMonth parseYearMonth(String date, String fieldName) {
        if (date == null || date.isBlank() || date.equals("yyyy-mm")) {
            throw new BadRequestException("Invalid " + fieldName + ". Format should be yyyy-MM or yyyy-MM-dd");
        }
        try {
            if (date.matches("\\d{4}-\\d{2}")) {
                return YearMonth.parse(date, YEAR_MONTH_FORMATTER);
            } else if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return YearMonth.from(LocalDate.parse(date));
            }
        } catch (DateTimeParseException | NullPointerException e) {
            // fall through
        }
        throw new BadRequestException("Invalid " + fieldName + ". Format should be yyyy-MM or yyyy-MM-dd");
    }
}


    
