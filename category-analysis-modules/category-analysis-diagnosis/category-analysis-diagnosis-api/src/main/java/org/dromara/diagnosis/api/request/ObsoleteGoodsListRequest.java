package org.dromara.diagnosis.api.request;

import lombok.Data;

import java.util.List;

@Data
public class ObsoleteGoodsListRequest {

    private String sessionId;
    private List<String> productStatus;
    private String firstSaleDate;
    private List<String> obsoleteProductType;
    private Integer obsoleteSku;
    private List<String> obsoleteType;
    private String abcType;
    private String currentAbc;
    private String compareAbc;
    private String currentGrossRole;
    private String compareGrossRole;
    private String currentGmroiRole;
    private String compareGmroiRole;
    private List<String> priceBandList;
    private List<String> brandList;
    private List<String> specList;
    private String tagType;
    private List<String> tagList;
    private Integer page;
    private Integer size;
    private String order;
    private String orderType;
}
