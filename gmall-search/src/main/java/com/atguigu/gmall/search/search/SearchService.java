package com.atguigu.gmall.search.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.bean.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author Administrator
 * @Date 2020/8/27 14:30
 * @Version 1.0
 */
@Service
public class SearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    public void createIndex(Long spuId){
        ResponseVo<List<SkuEntity>> skus = this.pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = skus.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {
            //把sku转换成goods对象
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();
                //查询spu搜素属性和值
                ResponseVo<List<SpuAttrValueEntity>> attrValueResp =
                        this.pmsClient.querySearchAttrValueBySpuId(skuEntity.getSpuId());
                List<SpuAttrValueEntity> attrValueEntities = attrValueResp.getData();
                List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                if (!CollectionUtils.isEmpty(attrValueEntities)) {
                    searchAttrValues = attrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        searchAttrValue.setAttrId(spuAttrValueEntity.getAttrId());
                        searchAttrValue.setAttrName(spuAttrValueEntity.getAttrName());
                        searchAttrValue.setAttrValue(spuAttrValueEntity.getAttrValue());
                        return searchAttrValue;
                    }).collect(Collectors.toList());
                }
                //查询sku搜素属性及值
                ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResp =
                        this.pmsClient.querySearchAttrValueBySkuId(skuEntity.getId());

                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResp.getData();
                List<SearchAttrValue> searchAttrSkuValues = new ArrayList<>();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    searchAttrSkuValues = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        searchAttrValue.setAttrId(skuAttrValueEntity.getAttrId());
                        searchAttrValue.setAttrName(skuAttrValueEntity.getAttrName());
                        searchAttrValue.setAttrValue(skuAttrValueEntity.getAttrValue());
                        return searchAttrValue;
                    }).collect(Collectors.toList());
                }
                searchAttrValues.addAll(searchAttrSkuValues);
                goods.setSearchAttrs(searchAttrValues);
                //查询品牌
                ResponseVo<BrandEntity> brandEntityResp
                        = this.pmsClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                if (brandEntity != null) {
                    goods.setBrandId(skuEntity.getBrandId());
                    goods.setBrandName(brandEntity.getName());
                }
                //查询分类
                ResponseVo<CategoryEntity> categoryEntityRes =
                        this.pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                CategoryEntity categoryEntity = categoryEntityRes.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(skuEntity.getCatagoryId());
                    goods.setCategoryName(categoryEntity.getName());
                }
                ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(spuId);
                SpuEntity spuEntity = spuEntityResponseVo.getData();
                goods.setCreateTime(spuEntity.getCreateTime());
                goods.setPrice(skuEntity.getPrice().doubleValue());
                goods.setSales(0l);
                goods.setSkuId(skuEntity.getId());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                //查询库存信息
                ResponseVo<List<WareSkuEntity>> wareSkuResp =
                        this.wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity ->
                            wareSkuEntity.getStock() > 0);
                    goods.setStore(flag);
                }
                goods.setTitle(skuEntity.getTitle());
                return goods;
            }).collect(Collectors.toList());
            //导入索引库
            this.goodsRepository.saveAll(goodsList);
        }
    }

    public SearchResponseVo search(SearchParamVo searchParamVo) {

        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, this.buildDsl(searchParamVo));
            SearchResponse searchresponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析结果集
            SearchResponseVo responseVo = this.parseResult(searchresponse);
            responseVo.setPageNum(searchParamVo.getPageNum());
            responseVo.setPageSize(searchParamVo.getPageSize());
            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //解析结果集
    private SearchResponseVo parseResult(SearchResponse searchresponse) {
        SearchResponseVo responseVo = new SearchResponseVo();
        SearchHits hits = searchresponse.getHits();
        //总命中的记录数
        responseVo.setTotal(hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            //获取内层hits的_source数据
            String goodsJson = hitsHit.getSourceAsString();
            //反序列化为goods对象
            Goods goods = JSON.parseObject(goodsJson, Goods.class);
            //获取高亮的title覆盖掉普通title
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            String highlightTitle = highlightField.getFragments()[0].toString();
            goods.setTitle(highlightTitle);
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);
        //聚合结果解析
        Map<String, Aggregation> aggregationMap = searchresponse.getAggregations().asMap();
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            List<BrandEntity> brands = buckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                Long brandId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                brandEntity.setId(brandId);
                //解析品牌名称的子聚合，获取品牌名称
                Map<String, Aggregation> brandAggregationMap =
                        ((Terms.Bucket) bucket).getAggregations().asMap();
                ParsedStringTerms brandNameAgg =
                        (ParsedStringTerms) brandAggregationMap.get("brandNameAgg");
                brandEntity.setName(brandNameAgg.getBuckets().get(0).getKeyAsString());
                ParsedStringTerms logoAgg =
                        (ParsedStringTerms) brandAggregationMap.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)) {
                    brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList());
            responseVo.setBrands(brands);
        }
        //解析聚合结果集，获取分类
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryIdAggBuckets)) {
            List<CategoryEntity> categoryEntities = categoryIdAggBuckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                Long categoryId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                categoryEntity.setId(categoryId);
                ParsedStringTerms categoryName =
                        ((Terms.Bucket) bucket).getAggregations().get("categoryNameAgg");
                categoryEntity.setName(categoryName.getBuckets().get(0).getKeyAsString());
                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(categoryEntities);
        }
        //解析结果集获取规格参数

        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)) {
            List<SearchResponseAttrVo> filters = attrIdAggBuckets.stream().map(bucket -> {
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();
                //规格参数id
                responseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                // 规格参数的名称"attrNameAgg"
                ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                responseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                //规格参数值
                ParsedTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueAggBuckets)) {
                    List<String> attrValues = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString)
                            .collect(Collectors.toList());
                    responseAttrVo.setAttrValues(attrValues);
                }
                return responseAttrVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(filters);
        }
        return responseVo;
    }

    private SearchSourceBuilder buildDsl(SearchParamVo paramVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String keyword = paramVo.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            //打广告todo
            return searchSourceBuilder;
        }
        //构建查询条件
        //bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        //过滤
        //品牌过滤
        List<Long> brandId = paramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }
        //分类过滤
        List<Long> cid = paramVo.getCid();
        if (!CollectionUtils.isEmpty(cid)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", cid));
        }
        //价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        //是否有货
        Boolean store = paramVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }
        //规格参数过滤props=5:高通-麒麟&props=6:骁龙865-硅谷1000
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                String[] attrs = StringUtils.split(":");
                if (attrs != null && attrs.length == 2) {
                    String attrId = attrs[0];
                    String attrValueString = attrs[1];
                    String[] attrValues = StringUtils.split(attrValueString, "-");
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attrId));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));
                }
            });
        }
        searchSourceBuilder.query(boolQueryBuilder);

        //构建排序 0-默认，得分降序；1-按价格升序；2-按价格降序；3-按创建时间降序；4-按销量降序
        Integer sort = paramVo.getSort();
        String field = "";
        SortOrder order = null;
        switch (sort) {
            case 1:
                field = "price";
                order = SortOrder.ASC;
                break;
            case 2:
                field = "price";
                order = SortOrder.DESC;
                break;
            case 3:
                field = "createTime";
                order = SortOrder.DESC;
                break;
            case 4:
                field = "sales";
                order = SortOrder.DESC;
                break;
            default:
                field = "_score";
                order = SortOrder.DESC;
                break;
        }
        searchSourceBuilder.sort(field, order);
        //构建分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        //构建高亮
        searchSourceBuilder.highlighter(
                new HighlightBuilder().
                        field("title").preTags("<font style='color:red'>")
                        .postTags("</font>"));
        //构建聚合
        //构建品牌聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        // 5.2. 构建分类聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //构建规格参数的嵌套聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));
        //构建过滤结果集
        searchSourceBuilder.fetchSource(new String[]{"skuId", "title", "price", "defaultImage","subTitle"}, null);
        System.out.println(searchSourceBuilder.toString());
        return searchSourceBuilder;
    }
}
