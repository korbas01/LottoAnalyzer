package yimscompany.lottoanalyzer.Components;

import java.util.Comparator;

import yimscompany.lottoanalyzer.BusinessObjects.LottoRecord;

/**
 * Created by shyim on 15-03-11.
 */
public class LottoRecordComparator implements Comparator<LottoRecord> {
    @Override
    public int compare(LottoRecord lhs, LottoRecord rhs) {
        return lhs.getDate().compareTo(rhs.getDate());
    }
}
