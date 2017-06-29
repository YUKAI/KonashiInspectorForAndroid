package com.uxxu.konashi.inspector.android;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uxxu.konashi.lib.Konashi;
import com.uxxu.konashi.lib.KonashiListener;
import com.uxxu.konashi.lib.KonashiManager;

import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.Promise;

import info.izumin.android.bletia.BletiaException;

/**
 * Created by kiryu on 7/27/15.
 */
public final class KonashiInfoFragment extends Fragment implements KonashiListener {

    private KonashiManager mKonashiManager;

    private TextView mNameTextView;
    private TextView mFirmTextView;
    private TextView mRssiTextView;
    private ProgressBar mRssiProgressBar;
    private TextView mBatteryTextView;
    private ProgressBar mBatteryProgressBar;
    private Button mReloadButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.title_konashiInfo));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_konashi_info, container, false);

        mNameTextView = (TextView) view.findViewById(R.id.nameTextView);

        mFirmTextView = (TextView) view.findViewById(R.id.firmTextView);

        mRssiTextView = (TextView) view.findViewById(R.id.rssiTextView);
        mRssiProgressBar = (ProgressBar) view.findViewById(R.id.rssiProgressBar);

        mBatteryTextView = (TextView) view.findViewById(R.id.batteryTextView);
        mBatteryProgressBar = (ProgressBar) view.findViewById(R.id.batteryProgressBar);

        mReloadButton = (Button) view.findViewById(R.id.reloadButton);
        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reload();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mKonashiManager = Konashi.getManager();
        mKonashiManager.addListener(this);
        reload();
    }

    @Override
    public void onDestroy() {
        mKonashiManager.removeListener(this);
        super.onDestroy();
    }

    private void reload() {
        if (!mKonashiManager.isReady()) {
            return;
        }

        mNameTextView.setText(mKonashiManager.getPeripheralName());

        mKonashiManager.getSoftwareRevision()
                .then(new DonePipe<String, Integer, BletiaException, Void>() {
                    @Override
                    public Promise<Integer, BletiaException, Void> pipeDone(String result) {
                        mFirmTextView.setText(String.format("Firmware: %s", result));
                        return mKonashiManager.getBatteryLevel();
                    }
                })
                .then(new DonePipe<Integer, Integer, BletiaException, Void>() {
                    @Override
                    public Promise<Integer, BletiaException, Void> pipeDone(Integer result) {
                        mBatteryTextView.setText(String.format("%d%%", result));
                        mBatteryProgressBar.setProgress(result);
                        return mKonashiManager.getSignalStrength();
                    }
                })
                .then(new DonePipe<Integer, Void, BletiaException, Void>() {
                    @Override
                    public Promise<Void, BletiaException, Void> pipeDone(Integer result) {
                        mRssiTextView.setText(String.format("%ddb", result));
                        mRssiProgressBar.setProgress(Math.abs(result));
                        return null;
                    }
                });
    }

    @Override
    public void onConnect(KonashiManager manager) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reload();
            }
        }, 1000);
    }

    @Override
    public void onDisconnect(KonashiManager manager) {

    }

    @Override
    public void onError(KonashiManager manager, BletiaException e) {

    }

    @Override
    public void onUpdatePioOutput(KonashiManager manager, int value) {

    }

    @Override
    public void onUpdateUartRx(KonashiManager manager, byte[] value) {

    }

    @Override
    public void onUpdateSpiMiso(KonashiManager manager, byte[] value) {

    }

    @Override
    public void onUpdateBatteryLevel(KonashiManager manager, int level) {

    }

    @Override
    public void onFindNoDevice(KonashiManager manager) {

    }

    @Override
    public void onConnectOtherDevice(KonashiManager manager) {

    }
}
