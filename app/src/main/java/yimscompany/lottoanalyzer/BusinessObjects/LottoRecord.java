package yimscompany.lottoanalyzer.BusinessObjects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * this class contains a winning number info. e.g.) winning nums , date , bonusnum and etc.
 * Created by shyim on 15-03-03.
 */
public class LottoRecord implements Parcelable {
    private ArrayList<Integer> mWinningNums;
    private Integer mBonusNum;
    private Integer mEncore;
    private String mDate;

    public LottoRecord(ArrayList<Integer> w, Integer b, Integer e, String d){
        mWinningNums = w;

        if(b.intValue() > 0) {
            mBonusNum = b;
        }
        if(e.intValue() > 0) {
            mEncore = e;
        }

        mDate = d;
    }

    public LottoRecord(Parcel in)
    {
        mWinningNums = (ArrayList<Integer>)in.readSerializable();
        Integer b = new Integer(in.readInt());
        if(b > 0) {
            mBonusNum = b;
        }

        Integer e = new Integer(in.readInt());
        if(e.intValue() > 0) {
            mEncore = e;
        }

        mDate = in.readString();
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mWinningNums);
        if(mBonusNum != null) {
            dest.writeInt(mBonusNum.intValue());
        }else{
            dest.writeInt(0);
        }
        if(mEncore != null) {
            dest.writeInt(mEncore.intValue());
        }else{
            dest.writeInt(0);
        }
        dest.writeString(mDate);

    }

    public ArrayList<Integer> getWinningNums() {
        return mWinningNums;
    }

    public String getDate() {
        return mDate;
    }

    public Integer getBonusNum() {
        return mBonusNum;
    }

    public Integer getEncore() {
        return mEncore;
    }


    public static final Parcelable.Creator<LottoRecord> CREATOR
            = new Parcelable.Creator<LottoRecord>() {
        public LottoRecord createFromParcel(Parcel in) {
            return new LottoRecord(in);
        }

        public LottoRecord[] newArray(int size) {
            return new LottoRecord[size];
        }
    };

}
