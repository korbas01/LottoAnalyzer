package yimscompany.lottoanalyzer.BusinessLogic;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by shyim on 15-02-02.
 * this is not just a random number generator, it also picks 6 numbers from user selected
 * numbers.
 */
public class RandomNumberGenerator {
    private int maxRange;
    private ArrayList<Integer> omitList;
    private Random randomNumGenerator;

    //it will generate random number between 1 and <max>
    public RandomNumberGenerator(int max){
        this.maxRange = max;
        this.randomNumGenerator = new Random(new Date().getTime());
        this.omitList = new ArrayList<Integer>();
    }

    //a set of number will not be generated
    public void setOmitNumbers(ArrayList<Integer> omitList)
    {
        this.omitList = omitList;
    }

    //generating <nums> of random numbers from the filtered pool
    public ArrayList<Integer> generateNumbers(int nums){
        ArrayList<Integer> result = new ArrayList<Integer>(nums);

        while(result.size() < nums ) {
            int r = this.randomNumGenerator.nextInt(this.maxRange)  +1;
            Integer randomNum = Integer.valueOf(r);

            if( !result.contains(randomNum) && !this.omitList.contains(randomNum)){
                result.add(randomNum);
            }
        }
        return result;
    }
}
