package yimscompany.lottoanalyzer.Components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 *
 * Created by shyim on 15-02-07.
 */
public class LottoAnalyzerDialogFragment extends DialogFragment {
    public interface LottoDialogFragmentListener {
        public void onReturnValue(boolean isOk);
    }

    public static final String DIALOG_MSG = "LottoAnalyzerDialogFragment_msg";
    public static final String DIALOG_POS_BTN_LABEL = "LottoAnalyzerDialogFragment_pos";
    public static final String DIALOG_NEG_BTN_LABEL = "LottoAnalyzerDialogFragment_neg";
    public static final String DIALOG_TYPE = "LottoAnalyzerDialogFragment_type";

    public static LottoAnalyzerDialogFragment newInstance(Bundle args) {
        LottoAnalyzerDialogFragment fragment = new LottoAnalyzerDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = this.getArguments();
        if(args.isEmpty())
        {
            throw new NullPointerException();
        }
        String message = args.getString(DIALOG_MSG);
        String btnPos = args.getString(DIALOG_POS_BTN_LABEL);
        String btnNeg = args.getString(DIALOG_NEG_BTN_LABEL);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message)
                .setPositiveButton(btnPos, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LottoDialogFragmentListener activity = (LottoDialogFragmentListener) getActivity();
                        activity.onReturnValue(true);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
