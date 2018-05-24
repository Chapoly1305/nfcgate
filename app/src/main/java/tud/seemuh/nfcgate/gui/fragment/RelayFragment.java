package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.nfc.NfcManager;

public class RelayFragment extends Fragment implements BaseFragment {
    // UI references
    View mTagWaiting;
    LinearLayout mSelector;
    TextView mLog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_relay, container, false);

        // setup
        mTagWaiting = v.findViewById(R.id.tag_wait);
        mSelector = v.findViewById(R.id.relay_selector);
        mLog = v.findViewById(R.id.relay_log);

        // selector setup
        v.<Button>findViewById(R.id.btn_reader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(true);
            }
        });
        v.<Button>findViewById(R.id.btn_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(false);
            }
        });

        setHasOptionsMenu(true);
        setSelectorVisible(true);
        return v;
    }

    @Override
    public String getTagName() {
        return "relay";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_relay, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // TODO:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when user selects reader or tag
     */
    private void onSelect(boolean reader) {
        // enable relay, connect to server, etc
        getNfc().enableRelayMode(reader);

        // toggle selector visibility
        setSelectorVisible(false);
    }

    /**
     * Shows or hides the selector
     */
    private void setSelectorVisible(boolean visible) {
        mSelector.setVisibility(visible ? View.VISIBLE : View.GONE);
        mTagWaiting.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    public NfcManager getNfc() {
        return ((MainActivity) getActivity()).getNfc();
    }
}
