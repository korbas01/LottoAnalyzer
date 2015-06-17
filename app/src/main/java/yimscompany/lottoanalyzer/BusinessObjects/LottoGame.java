package yimscompany.lottoanalyzer.BusinessObjects;

import java.io.Serializable;

/**
 * it describes what kind of game
 * Created by shyim on 15-02-15.
 */
public class LottoGame implements Serializable {
    private int mSetOfNums;  //how many numbers do you pick for a game?
    private boolean mHasBonusNum;
    private boolean mHasEncore;
    private String mProvince;
    private String mCountry;
    private String mName;    //game name, it will be used to assign a table name in DB.
    private int mMinRange; //min number(inculsive)
    private int mMaxRange; //max number(inclusive)
    private int mGameID; //it will be used to send a request to OLG

    public LottoGame(String n, String p, String c, int gameID, int nums, boolean bonus, boolean encore, int minRange, int maxRange) {
        mName = n;
        mProvince = p;
        mCountry = c;
        mGameID = gameID;
        mSetOfNums = nums;
        mHasBonusNum = bonus;
        mHasEncore = encore;
        mMinRange = minRange;
        mMaxRange = maxRange;

    }

    public int getSetOfNums() {
        return mSetOfNums;

    }

    public boolean getHasBonusNum() {

        return mHasBonusNum;
    }

    public boolean getHasEncore() {
        return mHasEncore;
    }

    public String getProvince() {

        return mProvince;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getName() {
        return mName;
    }

    public String getTableName() {
        return mName.replace(" ","");
    }

    public int getMinRange() {
        return mMinRange;
    }

    public int getMaxRange() {
        return mMaxRange;
    }

    public int getGameID() {return mGameID; }
}
