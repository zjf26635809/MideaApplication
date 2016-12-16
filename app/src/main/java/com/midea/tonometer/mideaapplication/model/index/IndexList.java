package com.midea.tonometer.mideaapplication.model.index;

import java.io.Serializable;
import java.util.List;

/**
 * author：ex_zhongjf on 2016-12-12 13:42
 * email：<jianfeng.zhong@partner.midea.com>
 */
public class IndexList implements Serializable{

    private List<IndexInfo> indexInfos;

    public List<IndexInfo> getIndexInfos() {
        return indexInfos;
    }

    public void setIndexInfos(List<IndexInfo> indexInfos) {
        this.indexInfos = indexInfos;
    }

    @Override
    public String toString() {
        if (indexInfos == null)
            return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indexInfos.size(); i++)
            sb.append(indexInfos.get(i).toString() + "\n");

        return sb.toString();
    }
}
