package com.sdsmdg.harjot.MusicDNA;


import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class EqualizerFragment extends Fragment {

    LineSet dataset;
    LineChartView chart;
    Paint paint;
    float[] points;

    ImageView spinnerDropDownIcon;

    boolean correctPosition = true;

    short numberOfFrequencyBands;
    LinearLayout mLinearLayout;

    SeekBar[] seekBarFinal = new SeekBar[5];

    AnalogController bassController, reverbController;

    static Spinner presetSpinner;

    static FrameLayout equalizerBlocker;

    static ShowcaseView showCase;

    public EqualizerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HomeActivity.isEqualizerEnabled = true;

        spinnerDropDownIcon = (ImageView) view.findViewById(R.id.spinner_dropdown_icon);
        spinnerDropDownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presetSpinner.performClick();
            }
        });

        presetSpinner = (Spinner) view.findViewById(R.id.equalizer_preset_spinner);

        equalizerBlocker = (FrameLayout) view.findViewById(R.id.equalizerBlocker);

        if (HomeActivity.isEqualizerEnabled) {
            equalizerBlocker.setVisibility(View.GONE);
        } else {
            equalizerBlocker.setVisibility(View.VISIBLE);
        }

        chart = (LineChartView) view.findViewById(R.id.lineChart);
        paint = new Paint();
        dataset = new LineSet();

        bassController = (AnalogController) view.findViewById(R.id.controllerBass);
        reverbController = (AnalogController) view.findViewById(R.id.controller3D);

        bassController.setLabel("BASS");
        reverbController.setLabel("3D");

        if (!HomeActivity.isEqualizerReloaded) {
            int x = (int) ((PlayerFragment.bassBoost.getRoundedStrength() * 19) / 1000);
            if (x == 0) {
                bassController.setProgress(1);
            } else {
                bassController.setProgress(x);
            }

            if (HomeActivity.y == 0) {
                reverbController.setProgress(1);
            } else {
                reverbController.setProgress(HomeActivity.y);
            }
        } else {
            int x = (int) ((HomeActivity.bassStrength * 19) / 1000);
            if (x == 0) {
                bassController.setProgress(1);
            } else {
                bassController.setProgress(x);
            }

            if (HomeActivity.y == 0) {
                reverbController.setProgress(1);
            } else {
                reverbController.setProgress(HomeActivity.y);
            }
        }

        bassController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                HomeActivity.bassStrength = (short) (((float) 1000 / 19) * (progress));
                PlayerFragment.bassBoost.setStrength(HomeActivity.bassStrength);
            }
        });

        reverbController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                HomeActivity.reverbPreset = (short) ((progress * 6) / 19);
                PlayerFragment.presetReverb.setPreset(HomeActivity.reverbPreset);
                HomeActivity.y = progress;
            }
        });

        mLinearLayout = (LinearLayout) view.findViewById(R.id.equalizerContainer);
//        mLinearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView equalizerHeading = new TextView(HomeActivity.ctx);
        equalizerHeading.setText("Equalizer");
        equalizerHeading.setTextSize(20);
        equalizerHeading.setGravity(Gravity.CENTER_HORIZONTAL);

        numberOfFrequencyBands = PlayerFragment.mEqualizer.getNumberOfBands();

        points = new float[numberOfFrequencyBands];

        final short lowerEqualizerBandLevel = PlayerFragment.mEqualizer.getBandLevelRange()[0];
        final short upperEqualizerBandLevel = PlayerFragment.mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < numberOfFrequencyBands; i++) {
            final short equalizerBandIndex = i;
            final TextView frequencyHeaderTextView = new TextView(HomeActivity.ctx);
            frequencyHeaderTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            frequencyHeaderTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            frequencyHeaderTextView.setTextColor(Color.parseColor("#FFFFFF"));
            frequencyHeaderTextView.setText((PlayerFragment.mEqualizer.getCenterFreq(equalizerBandIndex) / 1000) + "Hz");
//            mLinearLayout.addView(frequencyHeaderTextView);

            LinearLayout seekBarRowLayout = new LinearLayout(HomeActivity.ctx);
            seekBarRowLayout.setOrientation(LinearLayout.VERTICAL);

            TextView lowerEqualizerBandLevelTextView = new TextView(HomeActivity.ctx);
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            lowerEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
            lowerEqualizerBandLevelTextView.setText((lowerEqualizerBandLevel / 100) + "dB");

            TextView upperEqualizerBandLevelTextView = new TextView(HomeActivity.ctx);
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            upperEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
            upperEqualizerBandLevelTextView.setText((upperEqualizerBandLevel / 100) + "dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.weight = 1;

            SeekBar seekBar = new SeekBar(HomeActivity.ctx);
            TextView textView = new TextView(HomeActivity.ctx);
            switch (i) {
                case 0:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar1);
                    textView = (TextView) view.findViewById(R.id.textView1);
                    break;
                case 1:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar2);
                    textView = (TextView) view.findViewById(R.id.textView2);
                    break;
                case 2:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar3);
                    textView = (TextView) view.findViewById(R.id.textView3);
                    break;
                case 3:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar4);
                    textView = (TextView) view.findViewById(R.id.textView4);
                    break;
                case 4:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar5);
                    textView = (TextView) view.findViewById(R.id.textView5);
                    break;
            }
            seekBarFinal[i] = seekBar;
            seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#565656"), PorterDuff.Mode.SRC_IN));
            seekBar.setId(i);
//            seekBar.setLayoutParams(layoutParams);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

            textView.setText(frequencyHeaderTextView.getText());
            textView.setTextColor(Color.WHITE);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            if (HomeActivity.isEqualizerReloaded) {
                points[i] = HomeActivity.seekbarpos[i] - lowerEqualizerBandLevel;
                dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                seekBar.setProgress(HomeActivity.seekbarpos[i] - lowerEqualizerBandLevel);
            } else {
                points[i] = PlayerFragment.mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                seekBar.setProgress(PlayerFragment.mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
                HomeActivity.seekbarpos[i] = PlayerFragment.mEqualizer.getBandLevel(equalizerBandIndex);
                HomeActivity.isEqualizerReloaded = true;
            }
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    PlayerFragment.mEqualizer.setBandLevel(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
                    points[seekBar.getId()] = PlayerFragment.mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                    HomeActivity.seekbarpos[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                    dataset.updateValues(points);
                    chart.notifyDataUpdate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    presetSpinner.setSelection(0);
                    HomeActivity.presetPos = 0;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            /*seekBarRowLayout.addView(upperEqualizerBandLevelTextView);
            seekBarRowLayout.addView(seekBar);
            seekBarRowLayout.addView(lowerEqualizerBandLevelTextView);

            mLinearLayout.addView(seekBarRowLayout);*/

            equalizeSound();

        }

        paint.setColor(Color.parseColor("#555555"));
        paint.setStrokeWidth((float) (1.33 * HomeActivity.ratio));

        dataset.setColor(Color.parseColor("#FFA036"));
        dataset.setSmooth(true);
        dataset.setThickness(5);

        chart.setXAxis(false);
        chart.setYAxis(false);

        chart.setYLabels(AxisController.LabelPosition.NONE);
        chart.setGrid(ChartView.GridType.FULL, 7, 10, paint);

        chart.setAxisBorderValues(-300, 3300);

        chart.addData(dataset);
        chart.show();

        showCase = new ShowcaseView.Builder(getActivity())
                .blockAllTouches()
                .singleShot(4)
                .setStyle(R.style.CustomShowcaseTheme)
                .useDecorViewAsParent()
                .setTarget(new ViewTarget(R.id.showcase_view_equalizer, getActivity()))
                .setContentTitle("Presets")
                .setContentText("Use one of the available presets")
                .build();
        showCase.setButtonText("Next");
        showCase.overrideButtonClick(new View.OnClickListener() {
            int count1 = 0;

            @Override
            public void onClick(View v) {
                count1++;
                switch (count1) {
                    case 1:
                        showCase.setTarget(new ViewTarget(R.id.equalizerContainer, getActivity()));
                        showCase.setContentTitle("Equalizer Controls");
                        showCase.setContentText("Use the seekbars to control the Individual frequencies");
                        showCase.setButtonText("Next");
                        break;
                    case 2:
                        showCase.setTarget(new ViewTarget(R.id.controllerBass, getActivity()));
                        showCase.setContentTitle("Bass and Reverb");
                        showCase.setContentText("Use these controls to control Bass and Reverb");
                        showCase.setButtonText("Done");
                        break;
                    case 3:
                        showCase.hide();
                        break;
                }
            }

        });

    }

    public void equalizeSound() {
        ArrayList<String> equalizerPresetNames = new ArrayList<>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter = new ArrayAdapter<String>(HomeActivity.ctx,
                R.layout.spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equalizerPresetNames.add("Custom");

        for (short i = 0; i < PlayerFragment.mEqualizer.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(PlayerFragment.mEqualizer.getPresetName(i));
        }

        presetSpinner.setAdapter(equalizerPresetSpinnerAdapter);
        presetSpinner.setDropDownWidth((HomeActivity.screen_width * 3) / 4);
        if (HomeActivity.isEqualizerReloaded && HomeActivity.presetPos != 0) {
            correctPosition = false;
            presetSpinner.setSelection(HomeActivity.presetPos);
        }

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0 && correctPosition) {
                    Toast.makeText(HomeActivity.ctx, "called", Toast.LENGTH_SHORT).show();
                    PlayerFragment.mEqualizer.usePreset((short) (position - 1));
                    HomeActivity.presetPos = position;
                    short numberOfFreqBands = PlayerFragment.mEqualizer.getNumberOfBands();

                    final short lowerEqualizerBandLevel = PlayerFragment.mEqualizer.getBandLevelRange()[0];

                    for (short i = 0; i < numberOfFreqBands; i++) {
                        seekBarFinal[i].setProgress(PlayerFragment.mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel);
                        points[i] = PlayerFragment.mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel;
                        HomeActivity.seekbarpos[i] = PlayerFragment.mEqualizer.getBandLevel(i);
                        dataset.updateValues(points);
                        chart.notifyDataUpdate();
                    }
                } else {

                }

                correctPosition = true;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
