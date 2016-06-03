package com.mukera.sheket.client.controller.items;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.widget.*;

import com.mukera.sheket.client.LoaderId;
import com.mukera.sheket.client.R;
import com.mukera.sheket.client.controller.util.NumberFormatter;
import com.mukera.sheket.client.controller.transactions.TransactionActivity;
import com.mukera.sheket.client.data.SheketContract.*;
import com.mukera.sheket.client.models.SBranchItem;
import com.mukera.sheket.client.models.SItem;
import com.mukera.sheket.client.utility.PrefUtil;

/**
 * Created by gamma on 3/27/16.
 */
public class BranchItemFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private static final String KEY_CATEGORY_ID = "key_category_id";
    private static final String KEY_BRANCH_ID = "key_branch_id";

    private long mCategoryId;
    private long mBranchId;

    private ListView mBranchItemList;
    private BranchItemCursorAdapter mBranchItemAdapter;

    public static BranchItemFragment newInstance(long category_id, long branch_id) {
        Bundle args = new Bundle();

        BranchItemFragment fragment = new BranchItemFragment();
        args.putLong(KEY_CATEGORY_ID, category_id);
        args.putLong(KEY_BRANCH_ID, branch_id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mCategoryId = args.getLong(KEY_CATEGORY_ID);
        mBranchId = args.getLong(KEY_BRANCH_ID);
    }

    void startTransactionActivity(int action, long branch_id) {
        Intent intent = new Intent(getActivity(), TransactionActivity.class);
        intent.putExtra(TransactionActivity.LAUNCH_ACTION_KEY, action);
        intent.putExtra(TransactionActivity.LAUNCH_BRANCH_ID_KEY, branch_id);
        startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_branch_item, container, false);

        FloatingActionButton buyAction, sellAction;

        buyAction = (FloatingActionButton) rootView.findViewById(R.id.float_btn_item_list_buy);
        buyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTransactionActivity(TransactionActivity.LAUNCH_TYPE_BUY, mBranchId);
            }
        });
        sellAction = (FloatingActionButton) rootView.findViewById(R.id.float_btn_item_list_sell);
        sellAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTransactionActivity(TransactionActivity.LAUNCH_TYPE_SELL, mBranchId);
            }
        });

        mBranchItemList = (ListView) rootView.findViewById(R.id.branch_item_list_view);
        mBranchItemAdapter = new BranchItemCursorAdapter(getActivity());
        final AppCompatActivity activity = (AppCompatActivity)getActivity();
        mBranchItemAdapter.setListener(new BranchItemCursorAdapter.ItemSelectionListener() {
            @Override
            public void editItemLocationSelected(final SBranchItem branchItem) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                final ItemLocationDialog dialog = new ItemLocationDialog();
                dialog.setBranchItem(branchItem);
                dialog.setListener(new ItemLocationDialog.ItemLocationListener() {
                    @Override
                    public void cancelSelected() {
                        dialog.dismiss();
                    }

                    @Override
                    public void locationSelected(final String location) {
                        // the text didn't change, ignore it
                        if (TextUtils.equals(branchItem.item_location, location))
                            return;

                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                ContentValues values = new ContentValues();
                                values.put(BranchItemEntry.COLUMN_ITEM_LOCATION, location);

                                /**
                                 * If the branch item was in created state, we don't want to change it until
                                 * we sync with server. If we change it to updated state, it will create problems
                                 * as the server still doesn't know about it and the update will fail.
                                 */
                                if (branchItem.change_status != ChangeTraceable.CHANGE_STATUS_CREATED) {
                                    values.put(ChangeTraceable.COLUMN_CHANGE_INDICATOR, ChangeTraceable.CHANGE_STATUS_UPDATED);
                                }
                                getContext().getContentResolver().update(
                                        BranchItemEntry.buildBaseUri(PrefUtil.getCurrentCompanyId(getContext())),
                                        values,
                                        String.format("%s = ? AND %s = ?",
                                                BranchItemEntry.COLUMN_BRANCH_ID, BranchItemEntry.COLUMN_ITEM_ID),
                                        new String[]{
                                                String.valueOf(branchItem.branch_id),
                                                String.valueOf(branchItem.item_id)
                                        }
                                );
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        };
                        t.start();
                    }
                });
                dialog.show(fm, "Set Location");
            }
        });
        mBranchItemList.setAdapter(mBranchItemAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LoaderId.MainActivity.BRANCH_ITEM_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        long company_id = PrefUtil.getCurrentCompanyId(getContext());
        return new CursorLoader(getActivity(),
                BranchItemEntry.buildAllItemsInBranchUri(company_id, mBranchId),
                SBranchItem.BRANCH_ITEM_WITH_DETAIL_COLUMNS,
                ItemEntry._full(ItemEntry.COLUMN_CATEGORY_ID) + " = ?",
                new String[]{String.valueOf(mCategoryId)},
                BranchItemEntry._full(BranchItemEntry.COLUMN_ITEM_ID) + " ASC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBranchItemAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mBranchItemAdapter.swapCursor(null);
    }

    public static class BranchItemCursorAdapter extends android.support.v4.widget.CursorAdapter {
        public interface ItemSelectionListener {
            void editItemLocationSelected(SBranchItem branchItem);
        }

        private static class ViewHolder {
            TextView item_name;
            TextView item_code;
            TextView qty_remain;
            TextView item_loc;
            ImageButton edit_loc;

            public ViewHolder(View view) {
                item_name = (TextView) view.findViewById(R.id.list_item_text_view_b_item_name);
                item_code = (TextView) view.findViewById(R.id.list_item_text_view_b_item_code);
                qty_remain = (TextView) view.findViewById(R.id.list_item_text_view_b_item_qty);
                item_loc = (TextView) view.findViewById(R.id.list_item_text_view_b_item_loc);
                edit_loc = (ImageButton) view.findViewById(R.id.list_item_img_btn_b_edit_location);
            }
        }

        public ItemSelectionListener mListener;

        public void setListener(ItemSelectionListener listener) {
            mListener = listener;
        }

        public BranchItemCursorAdapter(Context context) {
            super(context, null);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_branch_item, parent, false);
            ViewHolder holder = new ViewHolder(view);

            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            final SBranchItem branchItem = new SBranchItem(cursor, true);
            SItem item = branchItem.item;

            holder.item_name.setText(item.name);
            String code;
            if (item.has_bar_code) {
                code = item.bar_code;
            } else {
                code = item.manual_code;
            }
            holder.item_code.setText(code);
            if (branchItem.item_location == null ||
                    branchItem.item_location.isEmpty()) {
                holder.item_loc.setVisibility(View.GONE);
            } else {
                holder.item_loc.setVisibility(View.VISIBLE);
                holder.item_loc.setText(branchItem.item_location);
            }
            holder.edit_loc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.editItemLocationSelected(branchItem);
                }
            });
            holder.qty_remain.setText(NumberFormatter.formatDoubleForDisplay(branchItem.quantity));
        }
    }
}
