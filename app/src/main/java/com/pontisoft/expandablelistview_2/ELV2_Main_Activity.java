package com.pontisoft.expandablelistview_2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ELV2_Main_Activity extends AppCompatActivity {

    private ExpandableListAdapter oea;
    private ArrayList<HashMap<String, Object>> ordermaplist = new ArrayList<HashMap<String, Object>>();
    private ExpandableListView mListView;
    private List<List<HashMap<String, Object>>> childlist = new ArrayList<List<HashMap<String, Object>>>();
    private List<HashMap<String, Object>> childobjlist = null;


    private HashMap<String, Object> makeOrder(int cnt, String name, double price, String id, boolean isorder) {
        HashMap<String, Object> ord = new HashMap<String, Object>();
        ord.put("count", String.format("%d",cnt));
        ord.put("articlename", name);
        ord.put("price", String.format("%.2f",price));
        ord.put("splitcount", "0");
        ord.put("id", id);
        ord.put("isorder", Boolean.toString(isorder));
        return ord;
    }

    private void initializeData() {
        ordermaplist.clear();
        childlist.clear();
        ordermaplist.add(makeOrder(2, "Capuccino", 8.00, "1", true));
        childobjlist = new ArrayList<HashMap<String, Object>>();
        childlist.add(childobjlist);
        ordermaplist.add(makeOrder(3, "Fritz Cola", 6.00, "2", true));
        childobjlist = new ArrayList<HashMap<String, Object>>();
        childlist.add(childobjlist);
        ordermaplist.add(makeOrder(1, "Rumpsteak 200g", 28.00, "3", true));
        childobjlist = new ArrayList<HashMap<String, Object>>();
        childobjlist.add(makeOrder(1, "medium", 0.00, "4", false));
        childobjlist.add(makeOrder(1, "Backkartoffel", 0.00, "5", false));
        childobjlist.add(makeOrder(1, "Sauerrahm", 0.00, "6", false));
        childobjlist.add(makeOrder(1, "Beilagensalat", 0.00, "7", false));
        childlist.add(childobjlist);
        ordermaplist.add(makeOrder(1, "New York Strip 200g", 38.00, "8", true));
        childobjlist = new ArrayList<HashMap<String, Object>>();
        childobjlist.add(makeOrder(1, "medium rare", 0.00, "9", false));
        childobjlist.add(makeOrder(1, "Backkartoffel", 0.00, "10", false));
        childobjlist.add(makeOrder(1, "Kräuterbutter", 0.00, "11", false));
        childobjlist.add(makeOrder(1, "Cesars Salad", 0.00, "12", false));
        childlist.add(childobjlist);
        ordermaplist.add(makeOrder(2, "Espresso", 5.00, "13", true));
        childobjlist = new ArrayList<HashMap<String, Object>>();
        childlist.add(childobjlist);
        ordermaplist.add(makeOrder(1, "Apollinaris medium 0,7", 6.00, "14", true));
        childobjlist = new ArrayList<HashMap<String, Object>>();
        childlist.add(childobjlist);
        ordermaplist.add(makeOrder(2, "Club Mate", 5.00, "15", true));
        childobjlist = new ArrayList<HashMap<String, Object>>();
        childlist.add(childobjlist);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elv2_main);
        initializeData();
        oea = new ExpandableListAdapter(this, ordermaplist, R.layout.list_group, new String[] {"count", "articlename", "price", "splitcount", "id", "isorder"},
                new int[] {R.id.COUNT_CELL, R.id.NAME_CELL, R.id.PRICE_CELL, R.id.SELECTED_COUNT_CELL},
                (List<? extends List<? extends Map<String, ?>>>) childlist, R.layout.list_child, new String[] {"count", "articlename", "price", "splitcount", "id", "isorder"},
                new int[] {R.id.COUNT_CELL, R.id.NAME_CELL, R.id.PRICE_CELL, R.id.SELECTED_COUNT_CELL});
        mListView = (ExpandableListView) findViewById(R.id.lvExp);
        mListView.setAdapter(oea);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_elv2_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
