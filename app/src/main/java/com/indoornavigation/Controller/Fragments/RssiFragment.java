package com.indoornavigation.Controller.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.indoornavigation.Controller.DroneController;
import com.indoornavigation.Model.BaseStation;
import com.indoor.navigation.indoornavigation.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RssiFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RssiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RssiFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RssiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RssiFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RssiFragment newInstance(String param1, String param2) {
        RssiFragment fragment = new RssiFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    DroneController.Listener mDroneControllerListener = new DroneController.Listener() {
        @Override
        public void checkPointReachedListener(Marker marker) {

        }

        @Override
        public void videoReceivedListener(ARControllerCodec codec) {

        }

        @Override
        public void onDroneConnectionChangedListener(boolean connected) {

        }

        @Override
        public void positionChangedListener(LatLng latLng, float bearing) {

        }

        @Override
        public void onWifiScanlistChanged(ArrayList<BaseStation> baseStations) {
            try{
                for (BaseStation bs : baseStations) {
                    writeData(bs.toString());
                }
            } catch (IOException e) {
                Log.e("csv writing", e.getMessage());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try{
            createCsvWriter();
        } catch (IOException e) {
            Log.e("csv writing", e.getMessage());
        }

        View view = inflater.inflate(R.layout.fragment_rssi, container, false);

        DroneController droneController = new DroneController(getContext());
        droneController.setListener(mDroneControllerListener);

        EditText txtNumber = (EditText) view.findViewById(R.id.txtNumber);

        Button btnStart = (Button) view.findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO start tracking
                }
            });
        }

        return view;
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {


        }
    };

    FileWriter fileWriter;

    private void writeData(String data) throws IOException {
        if (fileWriter != null) {
            fileWriter.write(data);
        }
    }

    private void createCsvWriter() throws IOException {
        String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String fileName = "AnalysisData.csv";
        String filePath = baseDir + "/indoorNavigation" + File.separator + fileName;
        File f = new File(filePath );

        fileWriter = new FileWriter(f, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            fileWriter.close();
        } catch (Exception e) {

        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
