package com.ibm.tone_analyzer;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneCategory;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;

import java.util.Locale;

/**
 * Class used to construct a UI to deliver information received from ToneAnalyzer to the user.
 */
class AnalyzerResultBuilder {

    // Minimum visibility of the UI Tonescore Tags.
    private static final double VISIBILITY_FLOOR = 0.2;

    private final ToneAnalysis analysis;
    private final MainActivity context;

    public AnalyzerResultBuilder (MainActivity context, ToneAnalysis analysis) {
        this.context = context;
        this.analysis = analysis;
    }

    /**
     * Dynamically constructs a LinearLayout with information from ToneAnalyzer.
     * @return A LinearLayout with a dynamic number of tone_category.xml each with a dynamic number
     * of image_tag.xml.
     */
    LinearLayout buildAnalyzerResultView() {
        LinearLayout analysisLayout = new LinearLayout(context);
        analysisLayout.setOrientation(LinearLayout.VERTICAL);

        /*
         * The result of ToneAnalyzer is a list of ToneCategories which group types of Tones together.
         * Each ToneCategory has a number of ToneScores which give a specific type of Tone as well as
         * a confidence score from the service.
         */
        for (ToneCategory category : analysis.getDocumentTone().getTones()) {
            // For each ToneCategory construct a layout with a label and a image_tag container.
            View categoryLayout = context.getLayoutInflater().inflate(R.layout.tone_category, null);

            TextView categoryLabel = (TextView)categoryLayout.findViewById(R.id.categoryNameLabel);
            categoryLabel.setText(category.getName() + ":");

            FlexboxLayout categoryTagContainer = (FlexboxLayout)categoryLayout.findViewById(R.id.categoryTagContainer);

            for (final ToneScore tone : category.getTones()) {
                // For each ToneScore construct an image_tag with its name and score, and add it to the container.
                TextView toneTagView = (TextView)context.getLayoutInflater().inflate(R.layout.image_tag, null);
                toneTagView.setText(tone.getName());

                // Adjust the visibility of the tag based on the service's returned confidence score.
                double score = tone.getScore() * (1 - VISIBILITY_FLOOR) + VISIBILITY_FLOOR;
                toneTagView.setAlpha((float)score);

                // Set an onclick listener that gives each image_tag a toggle between its name and its score.
                toneTagView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView label = (TextView)v;
                        String currentText = label.getText().toString();

                        if (currentText.equals(tone.getName())) {
                            // Freeze the width of the label so the dimensions don't change when the score is shown.
                            label.setMinWidth(label.getWidth());
                            label.setText(String.format(Locale.US, "%.1f", tone.getScore() * 100) + "%");
                        } else {
                            label.setText(tone.getName());
                        }
                    }
                });

                // Add the image_tag to the container.
                categoryTagContainer.addView(toneTagView);
            }


            // Add the full tone_category view to the overall LinearLayout to be returned.
            analysisLayout.addView(categoryLayout);
        }

        return analysisLayout;
    }
}