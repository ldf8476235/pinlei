package org.dromara.diagnosis.api.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.LegacyClassDoctorSummaryRequest;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySalesAndGrossResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySingleValueResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySkuSetResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummarySonClassSalesResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummaryTurnoverDaysAndStockResponse;
import org.dromara.diagnosis.api.response.LegacyClassDoctorSummaryVipSalesResponse;
import org.dromara.diagnosis.api.response.LegacyNodeResponse;
import org.dromara.diagnosis.application.service.LegacyClassDoctorSummaryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/classDoctorSummary")
public class LegacyClassDoctorSummaryController {

    private final LegacyClassDoctorSummaryService summaryService;

    @PostMapping("/querySkuSet")
    public LegacyNodeResponse<LegacyClassDoctorSummarySkuSetResponse> querySkuSet(@RequestBody LegacyClassDoctorSummaryRequest request) {
        return LegacyNodeResponse.ok(summaryService.querySkuSet(request));
    }

    @PostMapping("/querySalesAndGross")
    public LegacyNodeResponse<LegacyClassDoctorSummarySalesAndGrossResponse> querySalesAndGross(@RequestBody LegacyClassDoctorSummaryRequest request) {
        return LegacyNodeResponse.ok(summaryService.querySalesAndGross(request));
    }

    @PostMapping("/querySalesTurnoverRate")
    public LegacyNodeResponse<LegacyClassDoctorSummarySingleValueResponse> querySalesTurnoverRate(@RequestBody LegacyClassDoctorSummaryRequest request) {
        return LegacyNodeResponse.ok(summaryService.querySalesTurnoverRate(request));
    }

    @PostMapping("/queryTurnoverDaysAndStock")
    public LegacyNodeResponse<LegacyClassDoctorSummaryTurnoverDaysAndStockResponse> queryTurnoverDaysAndStock(@RequestBody LegacyClassDoctorSummaryRequest request) {
        return LegacyNodeResponse.ok(summaryService.queryTurnoverDaysAndStock(request));
    }

    @PostMapping("/querySonClassSales")
    public LegacyNodeResponse<LegacyClassDoctorSummarySonClassSalesResponse> querySonClassSales(@RequestBody LegacyClassDoctorSummaryRequest request) {
        return LegacyNodeResponse.ok(summaryService.querySonClassSales(request));
    }

    @PostMapping("/queryVipSales")
    public LegacyNodeResponse<LegacyClassDoctorSummaryVipSalesResponse> queryVipSales(@RequestBody LegacyClassDoctorSummaryRequest request) {
        return LegacyNodeResponse.ok(summaryService.queryVipSales(request));
    }
}
