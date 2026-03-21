package com.henheang.stock_flow_commerce.service.implement;

import com.henheang.stock_flow_commerce.exception.BadRequestException;
import com.henheang.stock_flow_commerce.model.distributor.DistributorHomepage;
import com.henheang.stock_flow_commerce.model.order.OrderChartByMonth;
import com.henheang.stock_flow_commerce.repository.DistributorHomepageRepository;
import com.henheang.stock_flow_commerce.service.DistributorHomepageService;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class DistributorHomepageServiceImp implements DistributorHomepageService {

    private  final DistributorHomepageRepository distributorHomepageRepository;

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM");
        private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public DistributorHomepageServiceImp(DistributorHomepageRepository distributorHomepageRepository) {
        this.distributorHomepageRepository = distributorHomepageRepository;
    }


    @Override
    public DistributorHomepage getNewOrder(Integer currentUserId) {
        Integer storeId= distributorHomepageRepository.getStoreId(currentUserId);
        DistributorHomepage distributorHomepage= new DistributorHomepage();

        Integer newOrder= distributorHomepageRepository.getNewOrder(storeId);
        distributorHomepage.setNewOrder(newOrder);

        Integer preparing= distributorHomepageRepository.getPreparing(storeId);
        distributorHomepage.setPreparing(preparing);

        Integer dispatch= distributorHomepageRepository.getDispatch(storeId);
        distributorHomepage.setDispatch(dispatch);

        Integer confirming= distributorHomepageRepository.getConfirming(storeId);
        distributorHomepage.setConfirming(confirming);

        Integer completed= distributorHomepageRepository.getCompleted(storeId);
        distributorHomepage.setCompleted(completed);

        return distributorHomepage;
    }



    @Override
    public OrderChartByMonth getTotalByMonth(Integer currentUserId , String startDate, String endDate) throws ParseException {

        YearMonth startYearMonth = parseYearMonth(startDate, "startDate");
        YearMonth endYearMonth = parseYearMonth(endDate, "endDate");
        Integer startYear = startYearMonth.getYear();
        Integer startMonth = startYearMonth.getMonthValue();
        Integer endYear = endYearMonth.getYear();
        Integer endMonth = endYearMonth.getMonthValue();
        // 1st check allow endDate have to bigger than startDate
        if (endYear - startYear < 0 ) {
            throw new BadRequestException("Opps, end date need to be higher than start date. Please input at least 1 month higher");
        }
        if(Objects.equals(endYear, startYear) && endMonth - startMonth <0){
            throw new BadRequestException("Opps, end date need to be higher than start date. Please input at least 1 month higher");
        }



        int monthFromYear = 0;

            // get month differential
        monthFromYear=(endYear-startYear) * 12;

        int nMonth = 1 + (endMonth - startMonth);
        Integer totalMonth = nMonth + monthFromYear;

        System.out.println(totalMonth);

        // convert String to date
        Date newStartDate = formatter.parse(startDate);
//        System.out.println(newStartDate);
        if (endYearMonth.isAfter(YearMonth.now())) {
            throw new BadRequestException("End date cannot be higher than today. ");
        }


        OrderChartByMonth orderChart = new OrderChartByMonth();


        Map<Integer, String> months = new HashMap<>();
        months.put(1, "JANUARY");
        months.put(2, "FEBRUARY");
        months.put(3, "MARCH");
        months.put(4, "APRIL");
        months.put(5, "MAY");
        months.put(6, "JUNE");
        months.put(7, "JULY");
        months.put(8, "AUGUST");
        months.put(9, "SEPTEMBER");
        months.put(10, "OCTOBER");
        months.put(11, "NOVEMBER");
        months.put(12, "DECEMBER");

        Integer storeId = distributorHomepageRepository.getStoreId(currentUserId);

            if(endYear - startYear < 2 ) {

                //get total order by number of months
                Integer finalTotalOrder = (distributorHomepageRepository.getTotalOrderByMonth(storeId, startDate, endDate) ==null ? 0 : distributorHomepageRepository.getTotalOrderByMonth(storeId, startDate, endDate) );
                orderChart.setTotalOrder(finalTotalOrder);

                //get total products import by number of months
                Integer finalTotalProductImport = (distributorHomepageRepository.getTotalProductImport(storeId, startDate, endDate) == null? 0 : distributorHomepageRepository.getTotalProductImport(storeId,startDate,endDate));
                orderChart.setTotalProductImport(finalTotalProductImport);

                //get total products sold by number of months
                Integer finalTotalProductSold =(distributorHomepageRepository.getTotalProductSold(storeId, startDate, endDate) ==null? 0 :  distributorHomepageRepository.getTotalProductSold(storeId, startDate, endDate)) ;
                orderChart.setTotalProductSold(finalTotalProductSold);

                //get total month  by number of months
//                List<Integer> finalTotalMonth = distributorHomepageRepository.getTotalMonth(storeId, startDate, endDate);


                List<String> listMonth = new ArrayList<>();
                List<Integer> orderEachMonth= new ArrayList<>();

                for (int i=0;i< totalMonth;i++) {
                    listMonth.add(months.get(startMonth));
                    orderEachMonth.add(distributorHomepageRepository.getTotalOrderEachMonth(storeId, startMonth , startYear) == null ? 0 : distributorHomepageRepository.getTotalOrderEachMonth(storeId, startMonth, startYear) );
                    startMonth = (startMonth+1) % 13 ; // increment and reset if it reaches 13
                    if (startMonth == 0) {
                        startMonth = 1;
                        startYear++;
                    }
                }

                orderChart.setMonth(listMonth);

                orderChart.setTotalOrderEachMonth(orderEachMonth);

            }
//            else {throw new BadRequestException("Please do not select more than 2 years.");}

        // if the user input more than 2 years
        if(endYear - startYear > 1 ) {

            //get total order by number of year
            Integer finalTotalOrderYear = (distributorHomepageRepository.getTotalOrderByYear(storeId, startYear, endYear)==null ? 0 : distributorHomepageRepository.getTotalOrderByYear(storeId, startYear, endYear) ) ;
            orderChart.setTotalOrder(finalTotalOrderYear);


            //get total products import by number of years
            Integer finalTotalProductImportByYear = distributorHomepageRepository.getTotalProductImportByYear(storeId, startYear, endYear) ==null ?0 : distributorHomepageRepository.getTotalProductImportByYear(storeId, startYear, endYear) ;
            orderChart.setTotalProductImport(finalTotalProductImportByYear);


            //get total products sold by number of years
            Integer finalTotalProductSoldByYear = distributorHomepageRepository.getTotalProductSoldByYear(storeId, startYear, endYear)==null ?0 : distributorHomepageRepository.getTotalProductSoldByYear(storeId, startYear, endYear);
            orderChart.setTotalProductSold(finalTotalProductSoldByYear);


            //get total order for each year
//            Integer finalTotalOrderEachYear = distributorHomepageRepository.getTotalOrderEachYear(storeId, startYear);
            Integer totalYear = 1+(endYear - startYear);

            List<String> yearAfterLoop = new ArrayList<>();
            List<Integer> orderEachYear = new ArrayList<>();


            for (int i = 0; i < totalYear; i++) {

                String yearss = Integer.toString(startYear);
                yearAfterLoop.add(yearss);
                orderEachYear.add(distributorHomepageRepository.getTotalOrderEachYear(storeId, startYear) == null ? 0 : distributorHomepageRepository.getTotalOrderEachYear(storeId, startYear));
                startYear++;
            }
            orderChart.setMonth(yearAfterLoop);

            orderChart.setTotalOrderEachMonth(orderEachYear);
        }

        return orderChart;
    }

    private YearMonth parseYearMonth(String date, String fieldName) {
        try {
            return YearMonth.parse(date, YEAR_MONTH_FORMATTER);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new BadRequestException("Invalid " + fieldName + ". Format should be yyyy-MM");
        }
    }


//    @Override
//    public OrderChartByYear getTotalByYear(Integer currentUserId, String startDate, String endDate) throws ParseException {
//
//        Integer storeId= distributorHomepageRepository.getStoreId(currentUserId);
//        OrderChartByYear orderChartByYear= new OrderChartByYear();
//
//        Integer startYear = Integer.parseInt(startDate.substring(0, 4));
//        Integer startMonth = Integer.parseInt(startDate.substring(5));
//
//
//        Integer endYear = Integer.parseInt(endDate.substring(0, 4));
//        Integer endMonth = Integer.parseInt(endDate.substring(5));
//
//        // 1st check allow endDate have to bigger than startDate
//        if (endYear - startYear < 0 ) {
//            throw new BadRequestException("End date need to be higher than start date. Please input at least 1 month higher");
//        }
//
//        Date newEndDate = formatter.parse(endDate);
//        System.out.println(newEndDate);
//        if (newEndDate.compareTo(new Date()) > 0) {
//            throw new BadRequestException("End date cannot be higher than today. ");
//        }
//
//
//        if(endYear - startYear > 1 ) {
//
//            //get total order by number of year
//            Integer finalTotalOrderYear = distributorHomepageRepository.getTotalOrderByYear(storeId, startDate, endDate);
//            orderChartByYear.setTotalOrder(finalTotalOrderYear);
//
//
//            //get total products import by number of years
//            Integer finalTotalProductImportByYear = distributorHomepageRepository.getTotalProductImportByYear(storeId, startDate, endDate);
//            orderChartByYear.setTotalProductImport(finalTotalProductImportByYear);
//
//
//            //get total products sold by number of years
//            Integer finalTotalProductSoldByYear = distributorHomepageRepository.getTotalProductSoldByYear(storeId, startDate, endDate);
//            orderChartByYear.setTotalProductSold(finalTotalProductSoldByYear);
//
//
//            //get total order for each year
//            Integer finalTotalOrderEachYear = distributorHomepageRepository.getTotalOrderEachYear(storeId, startYear);
//            Integer totalYear = 1+(endYear - startYear);
//
//            List<String> yearAfterLoop = new ArrayList<>();
//            List<Integer> orderEachYear = new ArrayList<>();
//
//            System.out.println(startYear);
//
//            for (int i = 0; i < totalYear; i++) {
//
//                String yearss = Integer.toString(startYear);
//                yearAfterLoop.add(yearss);
//                orderEachYear.add(distributorHomepageRepository.getTotalOrderEachYear(storeId, startYear) == null ? 0 : distributorHomepageRepository.getTotalOrderEachYear(storeId, startYear));
//                startYear++;
//
//            }
//            orderChartByYear.setYear(yearAfterLoop);
//
//
//            orderChartByYear.setTotalOrderEachYear(orderEachYear);
//
//        }else {
//            throw new BadRequestException("Please select more than 2 years of date");
//        }
//
//        return orderChartByYear;
//    }
//

}
