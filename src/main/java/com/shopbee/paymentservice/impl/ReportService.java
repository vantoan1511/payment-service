package com.shopbee.paymentservice.impl;

import com.shopbee.paymentservice.dto.ReportPeriod;
import com.shopbee.paymentservice.dto.SaleReportRequest;
import com.shopbee.paymentservice.dto.SaleReportResponse;
import com.shopbee.paymentservice.entity.Transaction;
import com.shopbee.paymentservice.external.order.OrderServiceClient;
import com.shopbee.paymentservice.external.order.dto.Order;
import com.shopbee.paymentservice.repository.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ApplicationScoped
public class ReportService {

    private final OrderServiceClient orderServiceClient;
    private final TransactionRepository transactionRepository;

    @Inject
    public ReportService(@RestClient OrderServiceClient orderServiceClient,
                         TransactionRepository transactionRepository) {
        this.orderServiceClient = orderServiceClient;
        this.transactionRepository = transactionRepository;
    }

    public SaleReportResponse getSaleReport(SaleReportRequest saleReportRequest) {
        ReportPeriod period = Optional.ofNullable(saleReportRequest)
                .map(SaleReportRequest::getPeriod)
                .map(ReportPeriod::fromCode)
                .orElse(ReportPeriod.MONTHLY);
        int year = Optional.ofNullable(saleReportRequest).map(SaleReportRequest::getYear).orElse(Year.now().getValue());
        List<Transaction> transactions = transactionRepository.findSuccessByYear(year);

        List<BigDecimal> salesData;
        switch (period) {
            case DAILY -> salesData = getDailyTotalAmounts(transactions, year);
            case WEEKLY -> salesData = getWeeklyTotalAmounts(transactions, year);
            case QUARTERLY -> salesData = getQuarterlyTotalAmounts(transactions);
            default -> salesData = getMonthlyTotalAmounts(transactions);
        }

        return SaleReportResponse.builder()
                .period(period)
                .year(year)
                .labels(getLabels(period))
                .data(salesData)
                .build();
    }


    public List<BigDecimal> getMonthlyTotalAmounts(List<Transaction> transactions) {
        Map<Month, BigDecimal> monthlyTotals = new EnumMap<>(Month.class);
        Stream.of(Month.values()).forEach(month -> monthlyTotals.put(month, BigDecimal.ZERO));

        transactions.forEach(transaction -> {
            Month month = transaction.getCreatedAt().getMonth();
            BigDecimal totalAmount = getTotalAmount(transaction.getOrderId());
            monthlyTotals.put(month, monthlyTotals.get(month).add(totalAmount));
        });

        return Stream.of(Month.values())
                .map(monthlyTotals::get)
                .collect(Collectors.toList());
    }

    public List<BigDecimal> getDailyTotalAmounts(List<Transaction> transactions, int year) {
        Map<Integer, BigDecimal> dailyTotals = IntStream.rangeClosed(1, 365)
                .boxed()
                .collect(Collectors.toMap(day -> day, day -> BigDecimal.ZERO));

        transactions.forEach(transaction -> {
            int dayOfYear = transaction.getCreatedAt().getDayOfYear();
            BigDecimal totalAmount = getTotalAmount(transaction.getOrderId());
            dailyTotals.put(dayOfYear, dailyTotals.get(dayOfYear).add(totalAmount));
        });

        return IntStream.rangeClosed(1, 365)
                .mapToObj(dailyTotals::get)
                .collect(Collectors.toList());
    }

    public List<BigDecimal> getWeeklyTotalAmounts(List<Transaction> transactions, int year) {
        Map<Integer, BigDecimal> weeklyTotals = IntStream.rangeClosed(1, 52)
                .boxed()
                .collect(Collectors.toMap(week -> week, week -> BigDecimal.ZERO));
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        transactions.forEach(transaction -> {
            int weekOfYear = transaction.getCreatedAt().get(weekFields.weekOfYear());
            BigDecimal totalAmount = getTotalAmount(transaction.getOrderId());
            weeklyTotals.put(weekOfYear, weeklyTotals.get(weekOfYear).add(totalAmount));
        });

        return IntStream.rangeClosed(1, 52)
                .mapToObj(weeklyTotals::get)
                .collect(Collectors.toList());
    }

    public List<BigDecimal> getQuarterlyTotalAmounts(List<Transaction> transactions) {
        Map<Integer, BigDecimal> quarterlyTotals = new HashMap<>(Map.of(
                1, BigDecimal.ZERO, 2, BigDecimal.ZERO, 3, BigDecimal.ZERO, 4, BigDecimal.ZERO
        ));

        transactions.forEach(transaction -> {
            int quarter = (transaction.getCreatedAt().getMonthValue() - 1) / 3 + 1;
            BigDecimal totalAmount = getTotalAmount(transaction.getOrderId());
            quarterlyTotals.put(quarter, quarterlyTotals.get(quarter).add(totalAmount));
        });

        return IntStream.rangeClosed(1, 4)
                .mapToObj(quarterlyTotals::get)
                .collect(Collectors.toList());
    }

    private BigDecimal getTotalAmount(long orderId) {
        Order order = orderServiceClient.getOrderById(orderId);
        if (order != null) {
            return order.getTotalAmount();
        }
        return BigDecimal.ZERO;
    }

    private List<String> getLabels(ReportPeriod period) {
        return switch (period) {
            case DAILY -> generateDailyLabels();
            case WEEKLY -> generateWeeklyLabels();
            case MONTHLY -> List.of("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC");
            case QUARTERLY -> List.of("Q1", "Q2", "Q3", "Q4");
        };
    }

    private List<String> generateDailyLabels() {
        List<String> dailyLabels = new ArrayList<>();
        for (int day = 1; day <= 365; day++) {
            dailyLabels.add(String.valueOf(day));
        }
        return dailyLabels;
    }

    private List<String> generateWeeklyLabels() {
        List<String> weeklyLabels = new ArrayList<>();
        for (int week = 1; week <= 52; week++) {
            weeklyLabels.add("W" + week);
        }
        return weeklyLabels;
    }


}
