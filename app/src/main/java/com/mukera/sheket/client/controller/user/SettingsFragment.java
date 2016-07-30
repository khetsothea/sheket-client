package com.mukera.sheket.client.controller.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mukera.sheket.client.MainActivity;
import com.mukera.sheket.client.R;
import com.mukera.sheket.client.controller.ListUtils;
import com.mukera.sheket.client.controller.admin.CompanyFragment;
import com.mukera.sheket.client.controller.navigation.BaseNavigation;
import com.mukera.sheket.client.data.AndroidDatabaseManager;

/**
 * Created by fuad on 7/30/16.
 */
public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        ListView listSettings = (ListView) rootView.findViewById(R.id.setting_list_view_main);
        final SettingsAdapter adapter = new SettingsAdapter(getContext());
        listSettings.setAdapter(adapter);
        adapter.add(BaseNavigation.StaticNavigationOptions.OPTION_COMPANIES);
        adapter.add(BaseNavigation.StaticNavigationOptions.OPTION_USER_PROFILE);
        adapter.add(BaseNavigation.StaticNavigationOptions.OPTION_DEBUG);

        listSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Integer item = adapter.getItem(position);
                Fragment fragment = null;
                switch (item) {
                    case BaseNavigation.StaticNavigationOptions.OPTION_COMPANIES:
                        fragment = new CompanyFragment();
                        break;
                    case BaseNavigation.StaticNavigationOptions.OPTION_USER_PROFILE:
                        fragment = new ProfileFragment();
                        break;
                    case BaseNavigation.StaticNavigationOptions.OPTION_DEBUG:
                        startActivity(new Intent(getContext(), AndroidDatabaseManager.class));
                        break;
                }
                if (fragment != null) {
                    getActivity().getSupportFragmentManager().beginTransaction().
                            replace(R.id.main_fragment_container, fragment).
                            addToBackStack(null).
                            commit();
                }
            }
        });
        ListUtils.setDynamicHeight(listSettings);

        View logoutView = rootView.findViewById(R.id.settings_log_out);
        logoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity)getActivity();
                activity.onNavigationOptionSelected(BaseNavigation.StaticNavigationOptions.OPTION_LOG_OUT);
            }
        });

        return rootView;
    }

    static class SettingsAdapter extends ArrayAdapter<Integer> {
        public SettingsAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Integer item = getItem(position);

            SettingViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_item_settings, parent, false);
                holder = new SettingViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (SettingViewHolder) convertView.getTag();
            }

            holder.name.setText(BaseNavigation.StaticNavigationOptions.sEntityAndIcon.get(item).first);
            holder.icon.setImageResource(BaseNavigation.StaticNavigationOptions.sEntityAndIcon.get(item).second);

            return convertView;
        }

        static class SettingViewHolder {
            TextView name;
            ImageView icon;

            public SettingViewHolder(View view) {
                name = (TextView) view.findViewById(R.id.list_item_settings_text);
                icon = (ImageView) view.findViewById(R.id.list_item_settings_icon);
                view.setTag(this);
            }
        }
    }
}
