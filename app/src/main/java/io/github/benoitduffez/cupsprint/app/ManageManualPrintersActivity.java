package io.github.benoitduffez.cupsprint.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.benoitduffez.cupsprint.R;

public class ManageManualPrintersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_manual_printers);

        ListView printersList = (ListView) findViewById(R.id.manage_printers_list);
        View emptyView = findViewById(R.id.manage_printers_empty);

        // Build adapter
        final SharedPreferences prefs = getSharedPreferences(AddPrintersActivity.SHARED_PREFS_MANUAL_PRINTERS, Context.MODE_PRIVATE);
        int numPrinters = prefs.getInt(AddPrintersActivity.PREF_NUM_PRINTERS, 0);
        List<ManualPrinterInfo> printers = getPrinters(prefs, numPrinters);
        final ManualPrintersAdapter adapter = new ManualPrintersAdapter(this, R.layout.manage_printers_list_item, printers);

        // Setup adapter with click to remove
        printersList.setAdapter(adapter);
        printersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SharedPreferences.Editor editor = prefs.edit();
                int numPrinters = prefs.getInt(AddPrintersActivity.PREF_NUM_PRINTERS, 0);
                editor.putInt(AddPrintersActivity.PREF_NUM_PRINTERS, numPrinters - 1);
                editor.remove(AddPrintersActivity.PREF_NAME + position);
                editor.remove(AddPrintersActivity.PREF_URL + position);
                editor.apply();
                adapter.removeItem(position);
            }
        });

        emptyView.setVisibility(numPrinters <= 0 ? View.VISIBLE : View.GONE);
    }

    @NonNull
    private List<ManualPrinterInfo> getPrinters(SharedPreferences prefs, int numPrinters) {
        List<ManualPrinterInfo> printers = new ArrayList<>(numPrinters);
        String url, name;
        for (int i = 0; i < numPrinters; i++) {
            name = prefs.getString(AddPrintersActivity.PREF_NAME + i, null);
            url = prefs.getString(AddPrintersActivity.PREF_URL + i, null);
            printers.add(new ManualPrinterInfo(name, url));
        }
        return printers;
    }

    private static class ManualPrinterInfo {
        String url, name;

        public ManualPrinterInfo(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    private static class ManualPrinterInfoViews {
        TextView url, name;

        ManualPrinterInfoViews(TextView name, TextView url) {
            this.name = name;
            this.url = url;
        }
    }

    private static class ManualPrintersAdapter extends ArrayAdapter<ManualPrinterInfo> {
        public ManualPrintersAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ManualPrinterInfo> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ManualPrinterInfoViews views;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.manage_printers_list_item, parent, false);
                views = new ManualPrinterInfoViews(
                        (TextView) convertView.findViewById(R.id.manual_printer_name),
                        (TextView) convertView.findViewById(R.id.manual_printer_url)
                );
                convertView.setTag(views);
            } else {
                views = (ManualPrinterInfoViews) convertView.getTag();
            }

            ManualPrinterInfo info = getItem(position);
            if (info != null) {
                views.name.setText(info.name);
                views.url.setText(info.url);
            } else {
                throw new IllegalStateException("Manual printers list can't have invalid items");
            }

            return convertView;
        }

        public void removeItem(int position) {
            remove(getItem(position));
        }
    }
}
