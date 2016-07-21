package com.ywwynm.everythingdone.views.pickers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ywwynm.everythingdone.Def;
import com.ywwynm.everythingdone.R;
import com.ywwynm.everythingdone.adapters.BaseViewHolder;
import com.ywwynm.everythingdone.adapters.SingleChoiceAdapter;
import com.ywwynm.everythingdone.utils.DisplayUtil;

/**
 * Created by ywwynm on 2015/8/18.
 * ColorPicker for searching in ThingsActivity and
 * changing background of thing in DetailActivity
 */
public class ColorPicker extends PopupPicker {

    public static final String TAG = "ColorPicker";

    private int mType;
    private View.OnClickListener mOnClickListener;
    private ColorPickerAdapter mAdapter;
    private Rect mWindowRect;

    public ColorPicker(Context context, View parent, int type) {
        super(context, parent, R.style.ColorPickerAnimation);
        mType = type;
        ViewGroup.LayoutParams params = mRecyclerView.getLayoutParams();
        params.width = (int) (mScreenDensity * 128);
        if (mType == Def.PickerType.COLOR_HAVE_ALL) {
            params.height = (int) (mScreenDensity * 208);
        } else if (mType == Def.PickerType.COLOR_NO_ALL) {
            params.height = (int) (mScreenDensity * 168);
        }
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mType == Def.PickerType.COLOR_HAVE_ALL) {
                    return position == 0 ? 2 : 1;
                } else if (mType == Def.PickerType.COLOR_NO_ALL) {
                    return 1;
                }
                return 0;
            }
        });
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ColorPickerAdapter();
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mWindowRect = new Rect();
    }

    @Override
    public void show() {
        int xOffset = 0;
        mParent.getWindowVisibleDisplayFrame(mWindowRect);
        if (mType == Def.PickerType.COLOR_NO_ALL) {
            xOffset += (int) (mScreenDensity * 36);
            if (DisplayUtil.isTablet(mContext)) {
                xOffset += (int) (mScreenDensity * 12);
            }
        }
        if (mWindowRect.right != DisplayUtil.getDisplaySize(mContext).x) {
            xOffset += (int) (mScreenDensity * 40);
        }

        mPopupWindow.showAtLocation(mParent, Gravity.TOP | Gravity.END,
                xOffset, DisplayUtil.getStatusbarHeight(mContext));
    }

    public void setPickedListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public int getPickedIndex() {
        return mAdapter.getPickedPosition();
    }

    public int getPickedColor() {
        if (mType == Def.PickerType.COLOR_HAVE_ALL) {
            if (mAdapter.getPickedPosition() <= 0) {
                return -1979711488;
            } else {
                return mContext.getResources().getIntArray(R.array.thing)[mAdapter.getPickedPosition() - 1];
            }
        } else return mContext.getResources().getIntArray(R.array.thing)[mAdapter.getPickedPosition()];
    }

    @Override
    public void pickForUI(int index) {
        mAdapter.pick(index);
        if (mAnchor != null) {
            updateAnchor();
        }
    }

    @Override
    public void updateAnchor() {
        if (mType == Def.PickerType.COLOR_HAVE_ALL) {
            ((Drawable) mAnchor).mutate().setColorFilter(getPickedColor(), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private class ColorPickerAdapter extends SingleChoiceAdapter {

        static final int ALL_COLOR = 0;
        static final int NORMAL = 1;

        private LayoutInflater mInflater;

        public ColorPickerAdapter() {
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ALL_COLOR) {
                return new AllColorViewHolder(
                        mInflater.inflate(R.layout.color_picker_bt, parent, false));
            } else {
                return new FabViewHolder(
                        mInflater.inflate(R.layout.color_picker_fab, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
            if (mType == Def.PickerType.COLOR_HAVE_ALL) {
                if (position == 0) {
                    AllColorViewHolder holder = (AllColorViewHolder) viewHolder;
                    if (mPickedPosition == 0) {
                        holder.bt.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_checkbox_checked, 0, 0, 0);
                    } else {
                        holder.bt.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_checkbox_unchecked, 0, 0, 0);
                    }
                    holder.bt.setClickable(mPickedPosition != 0);
                } else {
                    setFab(viewHolder, position);
                }
            } else if (mType == Def.PickerType.COLOR_NO_ALL) {
                setFab(viewHolder, position);
            }
        }

        private void setFab(RecyclerView.ViewHolder viewHolder, int position) {
            FabViewHolder holder = (FabViewHolder) viewHolder;
            int index = mType == Def.PickerType.COLOR_HAVE_ALL ? position - 1 : position;
            holder.fab.setBackgroundTintList(ColorStateList.valueOf(mContext.getResources()
                    .getIntArray(R.array.thing)[index]));
            setFabMargin(holder.fab, index);
            if (mPickedPosition == position) {
                holder.fab.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_color_picked));
            } else {
                holder.fab.setImageDrawable(null);
            }
            holder.fab.setClickable(mPickedPosition != position);
        }

        private void setFabMargin(FloatingActionButton fab, int index) {
            int m8 = (int) (8 * mScreenDensity);
            int m4 = m8 >> 1;
            int m16 = m8 << 1;
            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) fab.getLayoutParams();
            switch (index) {
                case 0:
                    if (mType == Def.PickerType.COLOR_HAVE_ALL) {
                        params.setMargins(m16, m8, m8, m4);
                    } else {
                        params.setMargins(m16, m16, m8, m4);
                    }
                    break;
                case 1:
                    if (mType == Def.PickerType.COLOR_HAVE_ALL) {
                        params.setMargins(m8, m8, m16, m4);
                    } else {
                        params.setMargins(m8, m16, m16, m4);
                    }
                    break;
                case 2:
                    params.setMargins(m16, m4, m8, m4);
                    break;
                case 3:
                    params.setMargins(m8, m4, m16, m4);
                    break;
                case 4:
                    params.setMargins(m16, m4, m8, m16);
                    break;
                case 5:
                    params.setMargins(m8, m4, m16, m16);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            if (mType == Def.PickerType.COLOR_HAVE_ALL) {
                return 7;
            } else if (mType == Def.PickerType.COLOR_NO_ALL) {
                return 6;
            }
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (mType == Def.PickerType.COLOR_HAVE_ALL) {
                return position == 0 ? ALL_COLOR : NORMAL;
            } else if (mType == Def.PickerType.COLOR_NO_ALL) {
                return NORMAL;
            }
            return super.getItemViewType(position);
        }

        class AllColorViewHolder extends BaseViewHolder {

            final Button bt;

            public AllColorViewHolder(View itemView) {
                super(itemView);
                bt = f(R.id.bt_all_color);
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopupWindow.dismiss();
                        pickForUI(0);
                        if (mOnClickListener != null) {
                            mOnClickListener.onClick(v);
                        }
                    }
                });
            }
        }

        class FabViewHolder extends BaseViewHolder {

            final FloatingActionButton fab;

            public FabViewHolder(View itemView) {
                super(itemView);
                fab = f(R.id.fab_pick_color);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopupWindow.dismiss();
                        pickForUI(getAdapterPosition());
                        if (mOnClickListener != null) {
                            mOnClickListener.onClick(v);
                        }
                    }
                });
            }
        }
    }
}