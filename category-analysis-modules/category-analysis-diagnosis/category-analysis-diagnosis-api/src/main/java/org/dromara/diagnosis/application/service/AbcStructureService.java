package org.dromara.diagnosis.application.service;

import lombok.RequiredArgsConstructor;
import org.dromara.diagnosis.api.request.AbcParamUpdateRequest;
import org.dromara.diagnosis.api.request.PrecomputeJobCreateRequest;
import org.dromara.diagnosis.api.response.AbcDetailsItemResponse;
import org.dromara.diagnosis.api.response.AbcImageItemResponse;
import org.dromara.diagnosis.api.response.AbcMatrixResponse;
import org.dromara.diagnosis.api.response.AbcParamUpdateResponse;
import org.dromara.diagnosis.api.response.AbcSalesListItemResponse;
import org.dromara.diagnosis.api.response.AbcSalesListResponse;
import org.dromara.diagnosis.api.response.AbcTypeParamResponse;
import org.dromara.diagnosis.api.response.PrecomputeJobResponse;
import org.dromara.diagnosis.application.config.DiagnosisCacheProperties;
import org.dromara.diagnosis.application.model.DiagnosisSessionCacheModel;
import org.dromara.diagnosis.common.exception.DiagnosisBizException;
import org.dromara.diagnosis.common.exception.DiagnosisErrorCode;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisAbcStructureMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisPrecomputeMapper;
import org.dromara.diagnosis.infrastructure.mapper.DiagnosisSnapshotMapper;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcBucketRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcMatrixRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcParamConfigRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcParamSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisAbcSkuRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisOverviewSnapshotRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeJobRow;
import org.dromara.diagnosis.infrastructure.model.DiagnosisPrecomputeWindowRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AbcStructureService {

    private static final String TENANT_ID = "000000";
    private static final Map<String, String> ORDER_BY_MAPPING = buildOrderByMapping();

    private final DiagnosisAbcStructureMapper abcStructureMapper;
    private final DiagnosisSnapshotMapper snapshotMapper;
    private final DiagnosisSessionCacheStore sessionCacheStore;
    private final DiagnosisCacheProperties cacheProperties;
    private final DiagnosisPrecomputeMapper precomputeMapper;
    private final PrecomputeJobService precomputeJobService;

    public List<AbcTypeParamResponse> getAbcParams(String sessionId) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisAbcParamSnapshotRow> rows = abcStructureMapper.selectParamSnapshot(TENANT_ID, session.getQueryHash(), overview.getDataVersion());
        if (rows == null || rows.isEmpty()) {
            List<DiagnosisAbcParamConfigRow> configs = abcStructureMapper.selectParamConfig(TENANT_ID);
            rows = new ArrayList<>();
            if (configs != null) {
                for (DiagnosisAbcParamConfigRow cfg : configs) {
                    DiagnosisAbcParamSnapshotRow row = new DiagnosisAbcParamSnapshotRow();
                    row.setAbcType(cfg.getAbcType());
                    row.setAbcTypeName(cfg.getAbcTypeName());
                    row.setSalesPer(cfg.getSalesPer());
                    row.setGrossPer(cfg.getGrossPer());
                    row.setSaleQuantityPer(cfg.getSaleQuantityPer());
                    row.setARate(cfg.getARate());
                    row.setBRate(cfg.getBRate());
                    row.setCRate(cfg.getCRate());
                    row.setASkuRate(cfg.getASkuRate());
                    row.setBSkuRate(cfg.getBSkuRate());
                    row.setCSkuRate(cfg.getCSkuRate());
                    rows.add(row);
                }
            }
        }
        List<AbcTypeParamResponse> responses = new ArrayList<>();
        for (DiagnosisAbcParamSnapshotRow row : rows) {
            AbcTypeParamResponse item = new AbcTypeParamResponse();
            item.setAbcType(row.getAbcType());
            item.setAbcTypeName(row.getAbcTypeName());
            item.setSalesPer(row.getSalesPer());
            item.setGrossPer(row.getGrossPer());
            item.setSalesQuantityPer(row.getSaleQuantityPer());
            item.setArate(row.getARate());
            item.setBrate(row.getBRate());
            item.setCrate(row.getCRate());
            item.setAskuRate(row.getASkuRate());
            item.setBskuRate(row.getBSkuRate());
            item.setCskuRate(row.getCSkuRate());
            responses.add(item);
        }
        return responses;
    }

    public List<AbcImageItemResponse> getAbcImage(String sessionId, String abcType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisAbcBucketRow> rows = abcStructureMapper.selectBucket(TENANT_ID, session.getQueryHash(), overview.getDataVersion(), abcType);
        List<AbcImageItemResponse> result = new ArrayList<>();
        for (DiagnosisAbcBucketRow row : rows) {
            AbcImageItemResponse item = new AbcImageItemResponse();
            item.setAbcType(row.getBucket());
            item.setCurrentSalesPer(row.getCurrentSalesPer());
            item.setCurrentSkuPer(row.getCurrentSkuPer());
            item.setCompareSalesPer(row.getCompareSalesPer());
            item.setCompareSkuPer(row.getCompareSkuPer());
            item.setSetSalesPer(row.getSetSalesPer());
            item.setSetSkuPer(row.getSetSkuPer());
            result.add(item);
        }
        return result;
    }

    public AbcMatrixResponse getAbcMatrix(String sessionId, String abcType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        DiagnosisAbcMatrixRow row = abcStructureMapper.selectMatrix(TENANT_ID, session.getQueryHash(), overview.getDataVersion(), abcType);
        AbcMatrixResponse response = new AbcMatrixResponse();
        if (row == null) {
            return response;
        }
        response.setAaNum(nvl(row.getAaNum()));
        response.setAbNum(nvl(row.getAbNum()));
        response.setAcNum(nvl(row.getAcNum()));
        response.setAnNum(nvl(row.getAnNum()));
        response.setAtNum(nvl(row.getAtNum()));
        response.setBaNum(nvl(row.getBaNum()));
        response.setBbNum(nvl(row.getBbNum()));
        response.setBcNum(nvl(row.getBcNum()));
        response.setBnNum(nvl(row.getBnNum()));
        response.setBtNum(nvl(row.getBtNum()));
        response.setCaNum(nvl(row.getCaNum()));
        response.setCbNum(nvl(row.getCbNum()));
        response.setCcNum(nvl(row.getCcNum()));
        response.setCnNum(nvl(row.getCnNum()));
        response.setCtNum(nvl(row.getCtNum()));
        return response;
    }

    public List<AbcDetailsItemResponse> getAbcDetails(String sessionId, String abcType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        List<DiagnosisAbcBucketRow> rows = abcStructureMapper.selectBucket(TENANT_ID, session.getQueryHash(), overview.getDataVersion(), abcType);
        BigDecimal totalMetric = BigDecimal.ZERO;
        int totalSku = 0;
        for (DiagnosisAbcBucketRow row : rows) {
            totalMetric = totalMetric.add(row.getCurrentSales() == null ? BigDecimal.ZERO : row.getCurrentSales());
            totalSku += nvl(row.getCurrentSku());
        }
        List<AbcDetailsItemResponse> result = new ArrayList<>();
        for (DiagnosisAbcBucketRow row : rows) {
            AbcDetailsItemResponse item = new AbcDetailsItemResponse();
            item.setAbcType(row.getBucket());
            item.setSetSalesPer(row.getSetSalesPer());
            item.setSetSales(totalMetric.multiply(row.getSetSalesPer()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            item.setSetSkuPer(row.getSetSkuPer());
            item.setSetSku(BigDecimal.valueOf(totalSku).multiply(row.getSetSkuPer()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            item.setCurrentSku(BigDecimal.valueOf(nvl(row.getCurrentSku())));
            item.setCurrentSkuPer(row.getCurrentSkuPer());
            item.setStockQuantity(row.getStockQuantity());
            item.setStockQuantityPer(row.getStockQuantityPer());
            item.setCompareSku(BigDecimal.valueOf(nvl(row.getCompareSku())));
            item.setCompareSkuPer(row.getCompareSkuPer());
            item.setChangeSku(BigDecimal.valueOf(nvl(row.getChangeSku())));
            item.setCurrentSales(row.getCurrentSales());
            result.add(item);
        }
        return result;
    }

    public AbcSalesListResponse getAbcSalesList(String sessionId,
                                                String abcType,
                                                List<String> status,
                                                String promotion,
                                                String currentAbc,
                                                String compareAbc,
                                                Integer page,
                                                Integer size,
                                                String order,
                                                String orderType) {
        DiagnosisSessionCacheModel session = getSession(sessionId);
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(sessionId, session);
        int actualPage = page == null || page < 1 ? 1 : page;
        int actualSize = size == null || size < 1 ? 10 : Math.min(size, 200);
        int offset = (actualPage - 1) * actualSize;
        String actualOrderBy = ORDER_BY_MAPPING.getOrDefault(order, "sales");
        String actualOrderType = "asc".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        List<String> statusList = status == null ? new ArrayList<>() : new ArrayList<>(status);
        statusList.remove("-1");
        Long total = abcStructureMapper.countSku(TENANT_ID, session.getQueryHash(), overview.getDataVersion(), abcType, currentAbc, compareAbc, promotion, statusList);
        List<DiagnosisAbcSkuRow> rows = abcStructureMapper.selectSkuPage(
            TENANT_ID, session.getQueryHash(), overview.getDataVersion(), abcType,
            currentAbc, compareAbc, promotion, statusList, actualOrderBy, actualOrderType, offset, actualSize);

        List<AbcSalesListItemResponse> records = new ArrayList<>();
        if (rows != null) {
            for (DiagnosisAbcSkuRow row : rows) {
                AbcSalesListItemResponse item = new AbcSalesListItemResponse();
                item.setProductNo(row.getProductNo());
                item.setProductName(row.getProductName());
                item.setProductStatus(row.getProductStatus());
                item.setProductStatusNo(row.getProductStatusNo());
                item.setStoreNum(row.getStoreNum());
                item.setCurrentAbc(row.getCurrentAbc());
                item.setCompareAbc(row.getCompareAbc());
                item.setContribution(row.getContribution());
                item.setContributionPer(row.getContributionPer());
                item.setSaleQuantity(row.getSaleQuantity());
                item.setSaleQuantityPsd(row.getSaleQuantityPsd());
                item.setSales(row.getSales());
                item.setSalesPer(row.getSalesPer());
                item.setSalesPsd(row.getSalesPsd());
                item.setGross(row.getGross());
                item.setGrossPer(row.getGrossPer());
                item.setGrossPsd(row.getGrossPsd());
                item.setGrossRate(row.getGrossRate());
                item.setStockQuantity(row.getStockQuantity());
                item.setTurnoverRate(row.getTurnoverRate());
                item.setTurnoverDays(row.getTurnoverDays());
                item.setStockSalesRate(row.getStockSalesRate());
                item.setContributionRate(row.getContributionRate());
                item.setGmroi(row.getGmroi());
                item.setSalesRate(row.getSalesRate());
                item.setActivity(row.getActivity());
                item.setFirstSaleDate(row.getFirstSaleDate());
                item.setNewProduct(row.getNewProduct());
                item.setKeyProduct(row.getKeyProduct());
                item.setSeasonableFlag(row.getSeasonableFlag());
                item.setSeasonableFlagName(row.getSeasonableFlagName());
                item.setSeasonableStartDate(row.getSeasonableStartDate());
                item.setSeasonableEndDate(row.getSeasonableEndDate());
                item.setClassNo(row.getClassNo());
                item.setClassName(row.getClassName());
                item.setClassLevel(row.getClassLevel());
                item.setProductBarcode(row.getProductBarcode());
                item.setBrandName(row.getBrandName());
                item.setSpec(row.getSpec());
                item.setInPrice(row.getInPrice());
                item.setSalesPrice(row.getSalesPrice());
                item.setProductVendorNo(row.getProductVendorNo());
                item.setProductVendorName(row.getProductVendorName());
                item.setProductVendorNoName(row.getProductVendorNoName());
                records.add(item);
            }
        }

        AbcSalesListResponse response = new AbcSalesListResponse();
        response.setRecords(records);
        response.setTotal(total == null ? 0L : total);
        response.setCurrent(actualPage);
        response.setSize(actualSize);
        long pages = (response.getTotal() + actualSize - 1) / actualSize;
        response.setPages((int) pages);
        return response;
    }

    public AbcParamUpdateResponse updateAbcParams(AbcParamUpdateRequest request) {
        if (request == null || request.getAbcList() == null || request.getAbcList().isEmpty()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "abcList is required");
        }
        for (AbcParamUpdateRequest.AbcParamItem item : request.getAbcList()) {
            BigDecimal a = item.getArate() == null ? BigDecimal.ZERO : item.getArate();
            BigDecimal b = item.getBrate() == null ? BigDecimal.ZERO : item.getBrate();
            BigDecimal c = item.getCrate() == null ? BigDecimal.ZERO : item.getCrate();
            if (a.add(b).add(c).compareTo(new BigDecimal("100")) != 0) {
                throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "arate + brate + crate must equal 100");
            }
            DiagnosisAbcParamConfigRow row = new DiagnosisAbcParamConfigRow();
            row.setTenantId(TENANT_ID);
            row.setAbcType(item.getAbcType());
            row.setAbcTypeName(item.getAbcTypeName());
            row.setSalesPer(item.getSalesPer());
            row.setGrossPer(item.getGrossPer());
            row.setSaleQuantityPer(item.getSalesQuantityPer());
            row.setARate(item.getArate());
            row.setBRate(item.getBrate());
            row.setCRate(item.getCrate());
            row.setASkuRate(item.getAskuRate());
            row.setBSkuRate(item.getBskuRate());
            row.setCSkuRate(item.getCskuRate());
            abcStructureMapper.upsertParamConfig(row);
        }

        DiagnosisSessionCacheModel session = getSession(request.getSessionId());
        DiagnosisOverviewSnapshotRow overview = resolveOverviewSnapshot(request.getSessionId(), session);
        DiagnosisPrecomputeJobRow latestJob = precomputeMapper.selectLatestJobByRequestHash(TENANT_ID, session.getQueryHash());
        if (latestJob == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "no historical precompute job found to retrigger");
        }

        PrecomputeJobCreateRequest jobRequest = new PrecomputeJobCreateRequest();
        jobRequest.setModule("DIAGNOSIS");
        jobRequest.setReadRangeType("CUSTOM");
        jobRequest.setReadStart(latestJob.getReadStart());
        jobRequest.setReadEnd(latestJob.getReadEnd());
        jobRequest.setCompareStart(overview.getCompareStart());
        jobRequest.setCompareEnd(overview.getCompareEnd());
        jobRequest.setWindowTypes(latestJob.getWindowTypes());
        jobRequest.setForceRebuild(Boolean.TRUE);
        jobRequest.setPriority(latestJob.getPriority() == null ? 5 : latestJob.getPriority());
        jobRequest.setRequestJson(latestJob.getRequestJson());
        PrecomputeJobResponse job = precomputeJobService.createJob(jobRequest);
        session.setJobId(job.getJobId());
        session.setDataVersion(null);
        sessionCacheStore.save(request.getSessionId(), session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));

        AbcParamUpdateResponse response = new AbcParamUpdateResponse();
        response.setJobId(job.getJobId());
        response.setStatus(job.getStatus());
        return response;
    }

    private DiagnosisSessionCacheModel getSession(String sessionId) {
        DiagnosisSessionCacheModel model = sessionCacheStore.get(sessionId);
        if (model == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session not found or expired");
        }
        if (model.getQueryHash() == null || model.getQueryHash().isBlank()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "session payload corrupted");
        }
        return model;
    }

    private DiagnosisOverviewSnapshotRow resolveOverviewSnapshot(String sessionId, DiagnosisSessionCacheModel session) {
        if (session.getDataVersion() != null && !session.getDataVersion().isBlank()) {
            DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
                TENANT_ID, session.getQueryHash(), session.getDataVersion());
            if (overview != null) {
                return overview;
            }
        }
        DiagnosisOverviewSnapshotRow byJobBinding = resolveBoundJobSnapshot(sessionId, session);
        if (byJobBinding != null) {
            return byJobBinding;
        }
        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectLatestOverviewByQuery(TENANT_ID, session.getQueryHash());
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                "no diagnosis snapshot found for current query; trigger precompute first");
        }
        if (overview.getDataVersion() != null && !overview.getDataVersion().isBlank()) {
            session.setDataVersion(overview.getDataVersion());
            sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
        }
        return overview;
    }

    private DiagnosisOverviewSnapshotRow resolveBoundJobSnapshot(String sessionId, DiagnosisSessionCacheModel session) {
        if (session.getJobId() == null) {
            return null;
        }
        DiagnosisPrecomputeJobRow job = precomputeMapper.selectJobById(TENANT_ID, session.getJobId());
        if (job == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "bound precompute job not found");
        }
        if (!"SUCCESS".equalsIgnoreCase(job.getStatusCode())) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                "session result not ready, precompute job status: " + job.getStatusCode());
        }
        List<DiagnosisPrecomputeWindowRow> windows = precomputeMapper.selectWindowsByJobId(TENANT_ID, session.getJobId());
        String dataVersion = null;
        if (windows != null) {
            for (DiagnosisPrecomputeWindowRow window : windows) {
                if (window != null && window.getDataVersion() != null && !window.getDataVersion().isBlank()) {
                    dataVersion = window.getDataVersion();
                }
            }
        }
        if (dataVersion == null || dataVersion.isBlank()) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT, "bound precompute job has no data version");
        }
        DiagnosisOverviewSnapshotRow overview = snapshotMapper.selectOverviewByQueryAndVersion(
            TENANT_ID, session.getQueryHash(), dataVersion);
        if (overview == null) {
            throw new DiagnosisBizException(DiagnosisErrorCode.INVALID_ARGUMENT,
                "bound precompute result not published, please retry later");
        }
        session.setDataVersion(dataVersion);
        sessionCacheStore.save(sessionId, session, Duration.ofMinutes(Math.max(1, cacheProperties.getResultTtlMinutes())));
        return overview;
    }

    private Integer nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static Map<String, String> buildOrderByMapping() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("storeNum", "store_num");
        map.put("saleQuantity", "sale_quantity");
        map.put("saleQuantityPsd", "sale_quantity_psd");
        map.put("sales", "sales");
        map.put("salesPer", "sales_per");
        map.put("salesPsd", "sales_psd");
        map.put("gross", "gross");
        map.put("grossPer", "gross_per");
        map.put("grossPsd", "gross_psd");
        map.put("grossRate", "gross_rate");
        map.put("productNo", "product_no");
        map.put("productName", "product_name");
        map.put("stockQuantity", "stock_quantity");
        map.put("turnoverRate", "turnover_rate");
        map.put("turnoverDays", "turnover_days");
        map.put("stockSalesRate", "stock_sales_rate");
        map.put("contributionRate", "contribution_rate");
        map.put("gmroi", "gmroi");
        map.put("salesRate", "sales_rate");
        map.put("activity", "activity");
        map.put("firstSaleDate", "first_sale_date");
        return map;
    }
}
