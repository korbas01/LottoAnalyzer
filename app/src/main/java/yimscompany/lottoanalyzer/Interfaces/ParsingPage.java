package yimscompany.lottoanalyzer.Interfaces;

import yimscompany.lottoanalyzer.Exceptions.InvalidConnectionException;

/**
 * Created by shyim on 15-02-17.
 */
public interface ParsingPage {
    public void ParsingPastWinningNumbers(String queryStr) throws InvalidConnectionException;
    public boolean IsUpToDate(String queryStr);
}
