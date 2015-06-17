package yimscompany.lottoanalyzer.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import yimscompany.lottoanalyzer.R;

/**
 * Created by shyim on 15-06-09.
 */
public class AlertDialogFragment extends DialogFragment {
    public static AlertDialogFragment newInstance(int titleResId, int contextResId){
        AlertDialogFragment af = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", titleResId);
        args.putInt("context", contextResId);
        af.setArguments(args);
        return af;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        int titleId = getArguments().getInt("title");
        int contextId = getArguments().getInt("context");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleId)
                .setMessage(contextId)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
