package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.model.distributor.DistributorHomepage;
import com.henheang.hphsar.model.order.OrderChartByMonth;
import com.henheang.hphsar.repository.DistributorHomepageRepository;
import com.henheang.hphsar.service.DistributorHomepageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class DistributorHomepageServiceImp implements DistributorHomepageService {

    private final DistributorHomepageRepository distributorHomepageRepository;

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private static final Map<Integer, String> MONTH_NAMES = Map.ofEntries(
            Map.entry(1,  "JANUARY"),  Map.entry(2,  "FEBRUARY"), Map.entry(3,  "MARCH"),
            Map.entry(4,  "APRIL"),    Map.entry(5,  "MAY"),      Map.entry(6,  "JUNE"),
            Map.entry(7,  "JULY"),     Map.entry(8,  "AUGUST"),   Map.entry(9,  "SEPTEMBER"),
            Map.entry(10, "OCTOBER"),  Map.entry(11, "NOVEMBER"), Map.entry(12, "DECEMBER")
    );

    public DistributorHomepageServiceImp(DistributorHomepageRepository distributorHomepageRepository) {
        this.distributorHomepageRepository = distributorHomepageRepository;
    }

    @Override
    public DistributorHomepage getNewOrder(Integer currentUserId) {
        Integer storeId = distributorHomepageRepository.getStoreId(currentUserId);
        return distributorHomepageRepository.getHomepageCounts(storeId);
    }

    @Override
    public OrderChartByMonth getTotalByMonth(Integer currentUserId, String startDate, String endDate) {
        YearMonth startYearMonth = parseYearMonth(startDate, "startDate");
        YearMonth endYearMonth   = parseYearMonth(endDate,   "endDate");

        if (endYearMonth.isBefore(startYearMonth)) {
            throw new BadRequestException("End date must be higher than start date. Please input at least 1 month higher.");
        }
        if (endYearMonth.isAfter(YearMonth.now())) {
            throw new BadRequestException("End date cannot be higher than today.");
        }

        int startYear = startYearMonth.getYear();
        int endYear   = endYearMonth.getYear();
        int yearDiff  = endYear - startYear;

        Integer storeId = distributorHomepageRepository.getStoreId(currentUserId);
        OrderChartByMonth orderChart = new OrderChartByMonth();

        if (yearDiff < 2) {
            Integer rawOrder  = distributorHomepageRepository.getTotalOrderByMonth(storeId, startDate, endDate);
            Integer rawImport = distributorHomepageRepository.getTotalProductImport(storeId, startDate, endDate);
            Integer rawSold   = distributorHomepageRepository.getTotalProductSold(storeId, startDate, endDate);

            orderChart.setTotalOrder(Objects.requireNonNullElse(rawOrder, 0));
            orderChart.setTotalProductImport(Objects.requireNonNullElse(rawImport, 0));
            orderChart.setTotalProductSold(Objects.requireNonNullElse(rawSold, 0));

            int totalMonth = (int) startYearMonth.until(endYearMonth.plusMonths(1), ChronoUnit.MONTHS);
            List<String>  listMonth      = new ArrayList<>();
            List<Integer> orderEachMonth = new ArrayList<>();
            YearMonth current = startYearMonth;

            for (int i = 0; i < totalMonth; i++) {
                listMonth.add(MONTH_NAMES.get(current.getMonthValue()));
                Integer rawEach = distributorHomepageRepository.getTotalOrderEachMonth(storeId, current.getMonthValue(), current.getYear());
                orderEachMonth.add(Objects.requireNonNullElse(rawEach, 0));
                current = current.plusMonths(1);
            }

            orderChart.setMonth(listMonth);
            orderChart.setTotalOrderEachMonth(orderEachMonth);

        } else {
            Integer rawOrder  = distributorHomepageRepository.getTotalOrderByYear(storeId, startYear, endYear);
            Integer rawImport = distributorHomepageRepository.getTotalProductImportByYear(storeId, startYear, endYear);
            Integer rawSold   = distributorHomepageRepository.getTotalProductSoldByYear(storeId, startYear, endYear);

            orderChart.setTotalOrder(Objects.requireNonNullElse(rawOrder, 0));
            orderChart.setTotalProductImport(Objects.requireNonNullElse(rawImport, 0));
            orderChart.setTotalProductSold(Objects.requireNonNullElse(rawSold, 0));

            int totalYear = yearDiff + 1;
            List<String>  yearLabels  = new ArrayList<>();
            List<Integer> orderEachYear = new ArrayList<>();

            for (int i = 0; i < totalYear; i++) {
                yearLabels.add(String.valueOf(startYear + i));
                Integer rawEach = distributorHomepageRepository.getTotalOrderEachYear(storeId, startYear + i);
                orderEachYear.add(Objects.requireNonNullElse(rawEach, 0));
            }

            orderChart.setMonth(yearLabels);
            orderChart.setTotalOrderEachMonth(orderEachYear);
        }

        return orderChart;
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