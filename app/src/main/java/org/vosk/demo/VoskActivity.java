// Copyright 2019 Alpha Cephei Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.vosk.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import org.vosk.demo.COMMANDREC;

import java.util.* ;
import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class VoskActivity extends Activity implements
        RecognitionListener {

    static private final int STATE_START = 0;
    static private final int STATE_READY = 1;
    static private final int STATE_DONE = 2;
    static private final int STATE_FILE = 3;
    static private final int STATE_MIC = 4;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private TextView moduletask;
    private TextView modulename;
    private TextView recievedcommand;
    COMMANDREC find_required_madule = new COMMANDREC();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);
        Log.v("result", "onCreate");
        // Setup layout
        moduletask = findViewById(R.id.txt_moduletask);
        modulename = findViewById(R.id.txt_madulename);
        recievedcommand = findViewById(R.id.txt_recievedcommand);

        setUiState(STATE_START);

        //findViewById(R.id.recognize_file).setOnClickListener(view -> recognizeFile());
        findViewById(R.id.recognize_mic).setOnClickListener(view -> recognizeMicrophone());
        ((ToggleButton) findViewById(R.id.pause)).setOnCheckedChangeListener((view, isChecked) -> pause(isChecked));

        LibVosk.setLogLevel(LogLevel.INFO);


        //Log.v("result123", "resulttt: " +find_required_madule.find_madule_and_object("Where is my book") );


        // Check if user has given permission to record audio, init the model after permission is granted
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModel();
        }
    }

    private void initModel() {
        Log.v("result", "initModel");
        StorageService.unpack(this, "model-en-us", "model",
                (model) -> {
                    this.model = model;
                    setUiState(STATE_READY);
                },
                (exception) -> setErrorState("Failed to unpack the model" + exception.getMessage()));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("result", "onRequestPermissionsResult");
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                initModel();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.v("result", "onDestroy");
        super.onDestroy();

        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResult(String hypothesis) {
        analyze_hypo(hypothesis);
       // resultView.append(hypothesis + "\n");

    }

    @Override
    public void onFinalResult(String hypothesis) {
        //resultView.append(hypothesis + "\n");
        setUiState(STATE_DONE);
        if (speechStreamService != null) {
            speechStreamService = null;
        }

    }

    @Override
    public void onPartialResult(String hypothesis) {
       // resultView.append(hypothesis + "\n");
    }

    @Override
    public void onError(Exception e) {
        setErrorState(e.getMessage());
    }

    @Override
    public void onTimeout() {
        Log.v("result", "timeout");
        setUiState(STATE_DONE);
    }

    private void setUiState(int state) {
        Log.v("result", "setUiState");
        switch (state) {
            case STATE_START:
                //resultView.setText(R.string.preparing);
                //resultView.setMovementMethod(new ScrollingMovementMethod());
                //findViewById(R.id.recognize_file).setEnabled(false);
                findViewById(R.id.recognize_mic).setEnabled(false);
                findViewById(R.id.pause).setEnabled((false));
                break;
            case STATE_READY:
                //resultView.setText(R.string.ready);
                ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
                //findViewById(R.id.recognize_file).setEnabled(true);
                findViewById(R.id.recognize_mic).setEnabled(true);
                findViewById(R.id.pause).setEnabled((false));
                break;
            case STATE_DONE:
                //((Button) findViewById(R.id.recognize_file)).setText(R.string.recognize_file);
                ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
                //findViewById(R.id.recognize_file).setEnabled(true);
                findViewById(R.id.recognize_mic).setEnabled(true);
                findViewById(R.id.pause).setEnabled((false));
                break;
            case STATE_FILE:
                //((Button) findViewById(R.id.recognize_file)).setText(R.string.stop_file);
                //resultView.setText(getString(R.string.starting));
                findViewById(R.id.recognize_mic).setEnabled(false);
                //findViewById(R.id.recognize_file).setEnabled(true);
                findViewById(R.id.pause).setEnabled((false));
                break;
            case STATE_MIC:
                ((Button) findViewById(R.id.recognize_mic)).setText(R.string.stop_microphone);
                //resultView.setText(getString(R.string.say_something));
                //findViewById(R.id.recognize_file).setEnabled(false);
                findViewById(R.id.recognize_mic).setEnabled(true);
                findViewById(R.id.pause).setEnabled((true));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    private void setErrorState(String message) {
        Log.v("result", "setErrorState");
        //resultView.setText(message);
        ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
        //findViewById(R.id.recognize_file).setEnabled(false);
        findViewById(R.id.recognize_mic).setEnabled(false);
    }

    private void recognizeFile() {
        Log.v("result", "recognizeFile");
        if (speechStreamService != null) {
            setUiState(STATE_DONE);
            speechStreamService.stop();
            speechStreamService = null;
        } else {
            setUiState(STATE_FILE);
            try {
                Recognizer rec = new Recognizer(model, 16000.f, "[\"one zero zero zero one\", " +
                        "\"oh zero one two three four five six seven eight nine\", \"[unk]\"]");

                InputStream ais = getAssets().open(
                        "10001-90210-01803.wav");
                if (ais.skip(44) != 44) throw new IOException("File too short");

                speechStreamService = new SpeechStreamService(rec, ais, 16000);
                speechStreamService.start(this);
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
    }

    private void recognizeMicrophone() {
        Log.v("result", "recognizeMicrophone");
        if (speechService != null) {
            setUiState(STATE_DONE);
            speechService.stop();
            speechService = null;
        } else {
            setUiState(STATE_MIC);
            try {
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
    }


    private void pause(boolean checked) {
        Log.v("result", "pause");
        if (speechService != null) {
            speechService.setPause(checked);
        }
    }

    private void analyze_hypo(String hypothesis){
        String answer;
        String[] res = hypothesis.split("\"");

        if(res[3].split(" ").length > 1){
            answer = find_required_madule.find_madule_and_object(res[3]);
            String[] result = answer.split("\\|");
            if(result.length > 1) {
                String MODULE_NAME = result[0];
                switch (MODULE_NAME){
                    case "Text Reading":
                        modulename.setText(result[0] + " module is running!");
                        moduletask.setText("Reading ...");
                        recievedcommand.setText("Recognized command: " + res[3]);
                        break;
                    case "Scene Description":
                        modulename.setText(result[0] + " module is running!");
                        moduletask.setText("Describing the scene");
                        recievedcommand.setText("Recognized command: " + res[3]);
                        break;
                case "Finding Object":
                    modulename.setText(result[0] + " module is running!");
                    moduletask.setText("Finding the "+ result[1]);
                    recievedcommand.setText("Recognized command: " + res[3]);
                    break;
                default:
                    modulename.setText("Appropriate Module");
                    moduletask.setText("What Needs To Be Done?");
                    recievedcommand.setText("Recieved Text From User");

            }
            }else {
            modulename.setText("No modules enabled");
            moduletask.setText("Repeat Your Command!");
            recievedcommand.setText("Recognized command: " + res[3]);
        }

    }else {
            modulename.setText("No modules enabled");
            moduletask.setText("Repeat Your Command!");
            recievedcommand.setText("Recognized command: " + res[3]);
    }
    }

}
