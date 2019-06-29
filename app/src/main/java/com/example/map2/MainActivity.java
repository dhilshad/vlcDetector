package com.example.map2;

import java.util.ArrayList;///for navigation

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


import android.os.Handler;

//////////////////////////classess for navigation////////////////////////////////////
class Edge {
    private int fromNodeIndex;
    private int toNodeIndex;
    private int length;
    public Edge(int fromNodeIndex, int toNodeIndex, int length) {
        this.fromNodeIndex = fromNodeIndex;
        this.toNodeIndex = toNodeIndex;
        this.length = length;
    }
    public int getFromNodeIndex() {
        return fromNodeIndex;
    }
    public int getToNodeIndex() {
        return toNodeIndex;
    }
    public int getLength() {
        return length;
    }
    // determines the neighbouring node of a supplied node, based on the two nodes connected by this edge
    public int getNeighbourIndex(int nodeIndex) {
        if (this.fromNodeIndex == nodeIndex) {
            return this.toNodeIndex;
        } else {
            return this.fromNodeIndex;
        }
    }
}


class Node {
    private int distanceFromSource = Integer.MAX_VALUE;
    private int route=0;
    private boolean visited;
    private ArrayList<Edge> edges = new ArrayList<Edge>(); // now we must create edges
    public int getDistanceFromSource() {
        return distanceFromSource;
    }
    public void setDistanceFromSource(int distanceFromSource) {
        this.distanceFromSource = distanceFromSource;
    }
    public int getRoute() {
        return route;
    }
    public void setRoute(int checkPoint){
        route=(route*10)+checkPoint;
    }

    public boolean isVisited() {
        return visited;
    }
    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    public ArrayList<Edge> getEdges() {
        return edges;
    }
    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }
}


class Graph {
    private Node[] nodes;
    private int noOfNodes;
    private Edge[] edges;
    private int noOfEdges;
    public Graph(Edge[] edges) {
        this.edges = edges;
        // create all nodes ready to be updated with the edges
        this.noOfNodes = calculateNoOfNodes(edges);
        this.nodes = new Node[this.noOfNodes];
        for (int n = 0; n < this.noOfNodes; n++) {
            this.nodes[n] = new Node();
        }
        // add all the edges to the nodes, each edge added to two nodes (to and from)
        this.noOfEdges = edges.length;
        for (int edgeToAdd = 0; edgeToAdd < this.noOfEdges; edgeToAdd++) {
            this.nodes[edges[edgeToAdd].getFromNodeIndex()].getEdges().add(edges[edgeToAdd]);
            this.nodes[edges[edgeToAdd].getToNodeIndex()].getEdges().add(edges[edgeToAdd]);
        }
    }
    private int calculateNoOfNodes(Edge[] edges) {
        int noOfNodes = 0;
        for (Edge e : edges) {
            if (e.getToNodeIndex() > noOfNodes)
                noOfNodes = e.getToNodeIndex();
            if (e.getFromNodeIndex() > noOfNodes)
                noOfNodes = e.getFromNodeIndex();
        }
        noOfNodes++;
        return noOfNodes;
    }
    // next video to implement the Dijkstra algorithm !!!
    public void calculateShortestDistances(int nxtNode) {


        int nextNode = nxtNode;
        this.nodes[nextNode].setDistanceFromSource(0);

        // visit every node
        for (int i = 0; i < this.nodes.length; i++) {
            // loop around the edges of current node
            ArrayList<Edge> currentNodeEdges = this.nodes[nextNode].getEdges();
            for (int joinedEdge = 0; joinedEdge < currentNodeEdges.size(); joinedEdge++) {
                int neighbourIndex = currentNodeEdges.get(joinedEdge).getNeighbourIndex(nextNode);
                // only if not visited
                if (!this.nodes[neighbourIndex].isVisited()) {
                    int tentative = this.nodes[nextNode].getDistanceFromSource() + currentNodeEdges.get(joinedEdge).getLength();
                    if (tentative < nodes[neighbourIndex].getDistanceFromSource()) {

                        nodes[neighbourIndex].setDistanceFromSource(tentative);
                        this.nodes[neighbourIndex].setRoute((this.nodes[nextNode].getRoute()*10)+nextNode);
                    }
                }
            }
            // all neighbours checked so node visited
            nodes[nextNode].setVisited(true);
            // next node must be with shortest distance
            nextNode = getNodeShortestDistanced();
        }
    }
    // now we're going to implement this method in next part !
    private int getNodeShortestDistanced() {
        int storedNodeIndex = 0;
        int storedDist = Integer.MAX_VALUE;
        for (int i = 0; i < this.nodes.length; i++) {
            int currentDist = this.nodes[i].getDistanceFromSource();
            if (!this.nodes[i].isVisited() && currentDist < storedDist) {
                storedDist = currentDist;
                storedNodeIndex = i;
            }
        }
        return storedNodeIndex;
    }
    // display result
    public int printResult(int dest) {

        int route=this.nodes[dest].getRoute()*10+dest;

        return route;

    }
    public Node[] getNodes() {
        return nodes;
    }
    public int getNoOfNodes() {
        return noOfNodes;
    }
    public Edge[] getEdges() {
        return edges;
    }
    public int getNoOfEdges() {
        return noOfEdges;
    }
}
///////////////////end of navigation classes/////////////////////////////////////

public class MainActivity extends AppCompatActivity {

    private static final String AUDIO_RECORDER_FOLDER = "Indoor Positioning System";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 48000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder = null;
    public int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    String filename=null;
    int num_of_bytes;
    TextView timer,txt;
    EditText x,y;
    ImageView spot,map;//spot is for locate icon and map for map
    Spinner spinner;

    ////////////cordinates/////////////
    int[] posX={505,285};    //change x position here to match the map(may change from phone to phone(due to screen size difference)
    int[] posY={235,355,500};//change y position here
    ///////////////////////////////

    ////////for navigation//////////////////////////////////////////////////
    boolean isuserpressedlocate=true; //true user press the locate button
    boolean navOn=false; //true if navigation is on
    int destination=1;
    static int starting=1;

    Graph g;///graph object in digijkatrs algorithm
    Button navigate;
    TextView route; //stores the id of nodes representing shortest path
    int[] pathX={1230,710}; ///change these values to change position of path lines drawn
                            // (may change from phone to phone(due to screen size difference)
    int[] pathY={375,530,620,1000};// change these values to change position of path lines drawn
    Edge[] edges = {
            new Edge(1, 2, 1), new Edge(1, 5, 1),
            new Edge(2, 3, 1),new Edge(2, 6, 1),
            new Edge(3, 4, 1),
            new Edge(5, 6, 1),
            new Edge(6, 7, 1),
            new Edge(7, 8, 1),
    };/////this array represent the graph of dijikstras algorithm

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

////////////////for navigation//////////////////////////////////////////////////////
        navigate=findViewById(R.id.nav_button);
        route=findViewById(R.id.textView3);

        ///////////////////spinner-for selecting destination/////////////////////////////////////////////////////////////////////

        spinner=(findViewById(R.id.spinner2));

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinner));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    destination=1;

                } else if (i == 1) {
                    destination=3;

                }else if (i == 2) {
                    destination=4;

                }else if (i == 3) {
                    destination=5;

                }else if (i == 4) {
                    destination=7;

                }else if (i == 5) {
                    destination=8;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        /////////////////////spinner ends///////////////////////////////////////////////////
////////////////////////navigation ends//////////////////////////////////////////////////////////

        spot=findViewById(R.id.imageView5);
        map=findViewById(R.id.imageView4);
        txt=findViewById(R.id.textView2);
        x=findViewById(R.id.editText);///for giving X coordinate for testing-means, plotting without receiving data
        y=findViewById(R.id.editText2);/// for giving Y coordinate for testing

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, 1);
        setButtonHandlers();
        bufferSize = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
       // txt.setText(String.valueOf(bufferSize));
    }

    private void setButtonHandlers() {
        (findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        (findViewById(R.id.btnStop)).setOnClickListener(btnClick);
        (findViewById(R.id.button2)).setOnClickListener(btnClick);
        (findViewById(R.id.nav_button)).setOnClickListener(btnClick);
        (findViewById(R.id.button3)).setOnClickListener(btnClick);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }
        filename=file.getAbsolutePath() + "/"  + AUDIO_RECORDER_TEMP_FILE+"1";
        return (filename);
    }

    String filepath;
    private String getTempFilename(){
        filepath = Environment.getExternalStorageDirectory().getPath();
        //txt.setText(filepath);
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);


        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

       if(tempFile.exists())
            tempFile.delete();


        return (file.getAbsolutePath() + "/" +
                "" + AUDIO_RECORDER_TEMP_FILE);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void startRecording(){
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

         recorder.startRecording();

        num_of_bytes=0;
        writeAudioDataToFile();
        copyWaveFile(getTempFilename(),getFilename());

    }

    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;



        try {
            os = new FileOutputStream(filename);

        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();

        }

        int read = 0;

        if(null != os){

                read = recorder.read(data, 0, bufferSize);
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
           // }

            try {
                os.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            stopRecording();

        }
    }

    private void stopRecording(){
        if(null != recorder){

            int i = recorder.getState();
            if(i==1)
                recorder.stop();
            recorder.release();
            recorder = null;

        }

        copyWaveFile(getTempFilename(),getFilename());

    }



    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;

        byte[] data = new byte[bufferSize];

        try {

            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);

            int start=0;
            boolean begning=true;
            byte datastream[]=new byte[200];


            while(in.read(data) != -1){////read received raw data from file

              ////finding start bi////////////
                if(begning) {///searching for start bits. if start bit is found begninng=false;

                   // int low =0;
                    int high=0;
                    for (int i = 3; i < 3000; i += 2) {//first 2 bytes had some error in most cases so we start reading from 3rd byte

                       if (data[i]<(byte)0)high++; ///if data is high value, it will be negative (msb=1).so it will be<0
                       else high=0;
                        //transmitter send one symbol for 125 microseconds. and our sampling rate is 48000. so 6 samples
                        //
                       if (high==24){////6*4=24  . 4 high bits with duration 125 microseconds are send from the tx.
                            start=i+1;
                            txt.setText("strt:"+String.valueOf(start));
                            break;
                       }

                    }
                    /////////end//////////////////////
                    ///////////////////////
                    int ones;
                    int bitcount=0;

                    boolean adjustment_needed;//to compensate for error in transmitter.some times,due to timing error, more than 6 samples come.
                                             //
                     boolean start_bit; // represent start of 6 bit sequence

                    for (int i = start; i < 3840-24; i = i + 24) {
                         ones=0;
                         start_bit =false;
                         adjustment_needed=false;


                        for (int j = 1; j < 12; j = j + 2) {
                            if (j==1) if (data[(i+j)]<0) start_bit = true;
                            if (data[(i+j)] < 0) ones++;////if data is high value it will be negative (msb=1).so it will be<(in 2s complement)
                        }

                        if (ones > 4) {
                            byte temp=0;
                            for (int j=13;j<24;j=j+2) if (data[i+j]<0) temp++;

                            if (temp>3){
                                i=3840;
                            }
                            else{
                                datastream[bitcount] = 1;
                                bitcount++;
                                if (!start_bit) adjustment_needed=true;
                            }

                        }
                        else
                         {
                            datastream[bitcount] = 0;
                            bitcount++;
                            if (start_bit) adjustment_needed=true;
                         }
                        if (adjustment_needed) i=i+2;

                    }


                }
                begning=false;
                int cordinate[]={0,0};

               ///////////////cordiante calculation with firsr 4 bit for column and 2nd 4  bits for row/////////
                for (int i=0;i<2;i++){
                    int mult=1;
                    int charecter=0;
                    for (int j=0;j<4;j++){
                        charecter=charecter+(datastream[(i*4)+j]*mult);
                        mult=mult*2;
                    }
                    cordinate[i]=charecter-1;
                }

                /////////////////////////end of temp cordinate calculation/////////////////////////////////////
                out.write(datastream);//store the read data

               // cordinate[0] = Integer.parseInt(x.getText().toString());
                //cordinate[1] = Integer.parseInt(y.getText().toString());

               // cordinate[0]=0;
                //cordinate[1]=2;
                if ((cordinate[0]<2)&&(cordinate[0]>=0)&&(cordinate[1]<3)&&((cordinate[1])>=0)){
                  spot.setVisibility(View.VISIBLE);
                  spot.setX(posX[cordinate[0]]);
                  spot.setY(posY[cordinate[1]]);
                  ///for navigation///////////
                  starting=((cordinate[0]*4)+(cordinate[1])+1);
                  if (cordinate[1]>0) starting++;//adjustmanet to override the middle point
              }
              else spot.setVisibility(View.INVISIBLE);

              txt.setText("Heart beat:"+cordinate[0]+"y= "+cordinate[1]+"starting"+starting);//for debugging
            }
            in.close();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/////////////////////////////////////////////////////////////////////////////////


    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override

        public void onClick(View v) {

            switch(v.getId()){
                case R.id.btnStart:{
                //    AppLog.logString("Start Recording");
                    map.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mall));
                    startRecording();

                    ////////timer for periodic location update////////
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        isuserpressedlocate=false;
                            findViewById(R.id.btnStart).performClick();
                            isuserpressedlocate =true;
                        }
                    }, 1000);

                    /////////////////////////////////////////
                    if (!isuserpressedlocate){
                        if (navOn) navigate.performClick();
                    }
                    else navOn=false;
                    break;
                }
                case R.id.btnStop:{
                    stopRecording();
                    break;
                }
                //////////////////////////////button2////////////////////
                case R.id.button2:{

                    spot.setX(pathX[1]);
                    spot.setY(pathY[3]);
                    break;
                }
                ///////////navigation button////////////////////////////
                case R.id.nav_button:{
                    navOn=true;
                    g=new Graph(edges);
                    g.calculateShortestDistances(starting);
                    txt.setText(String.valueOf(starting));
                    int path=g.printResult(destination);
                    route.setText(String.valueOf(path)); // let's try it !
                    drawpath(path);
                    break;
                }

                case R.id.button3:{
                    route.setText("h"); // let's try it !
                    break;
                }
            }

        }
    };


    ///////function for drawing path///////////////////////////////////
    private void drawpath(int path){

        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.mall);
        Bitmap mutableBitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int endx=0;
        int endy=0;
        int startx=0;
        int starty=0;

        // Line
        boolean isfirst=true;//to draw circle at terminal
        while((path/10)>0){


            Paint paint = new Paint();
            paint.setColor(Color.rgb(10,10,255));
            paint.setStrokeWidth(14);


            int start=(path%10)-1;
            path=path/10;
            int dest=(path%10)-1;


             startx = pathX[start/4];
             starty = pathY[start%4]+100;
             endx = pathX[dest/4];
             endy = pathY[dest%4]+100;
            canvas.drawLine(startx, starty, endx, endy, paint);
            if (isfirst){///draw circle at terminal

                paint.setStyle(Paint.Style.FILL);
                paint.setAntiAlias(false);

                canvas.drawCircle(startx, starty, 25, paint);
                isfirst=false;
            }
        }


        map.setImageBitmap(mutableBitmap);
    }
    ///////////////function drawing path ends///////////////
}




