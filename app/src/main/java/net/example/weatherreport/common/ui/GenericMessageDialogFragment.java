package net.example.weatherreport.common.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import net.example.weatherreport.R;

/**
 * Created by Carlo on 23/04/2017.
 */

public class GenericMessageDialogFragment extends DialogFragment {

    /**
     * Creates a new dialog
     * @param title resource id for the title
     * @param text resource id for the text (pass 0 for no text)
     * @param showOkButton show ok button (true/false)
     * @return the dialog
     */
    public static GenericMessageDialogFragment newInstance(int title, int text, boolean showOkButton) {
        GenericMessageDialogFragment frag = new GenericMessageDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("text", text);
        args.putBoolean("show_ok_button", showOkButton);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int title = getArguments().getInt("title");
        int text = getArguments().getInt("text");
        boolean showOkButton = getArguments().getBoolean("show_ok_button");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title);

        if (text != 0) {
            builder.setMessage(text);
        }

        if (showOkButton) {
            builder.setPositiveButton(R.string.alert_dialog_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dismiss();
                        }
                    }
            );
        }

        setCancelable(false);

        return builder.create();
    }

}
