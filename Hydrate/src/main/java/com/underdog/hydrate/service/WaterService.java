package com.underdog.hydrate.service;

import android.content.Context;

import com.underdog.hydrate.database.HydrateDAO;
import com.underdog.hydrate.util.DateUtil;

/**
 * Created by chandrasekhar.dandu on 6/20/2017.
 */

public class WaterService {

    private static WaterService waterService;

    private WaterService() {
    }

    public static WaterService getInstance() {
        if (waterService == null) {
            synchronized (WaterService.class) {
                if (waterService == null) {
                    waterService = new WaterService();
                }
            }
        }
        return waterService;
    }

    public void addWater(Context context, String date, String quantity) {
        // Save the water in DB
        if (DateUtil.getInstance().isToday(date)) {
            HydrateDAO.getInstance().addWater(System.currentTimeMillis(), quantity, context);
        } else {
            HydrateDAO.getInstance().addWater(DateUtil.getInstance().getThisTimeThatDay(date), quantity, context);

            //Update Target table if water is added to past dates
            HydrateDAO.getInstance().updateTargetStatus(context, DateUtil.getInstance().getTimeFromDate(date),false);
        }
    }

    public void updateWaterById(Context context, long rowId,
                                long timestamp, double quantity, String date) {
        HydrateDAO.getInstance().updateEvent(rowId,
                timestamp, quantity, context);
        if (!DateUtil.getInstance().isToday(date)) {
            //Update Target table if water is modified in past dates
            HydrateDAO.getInstance().updateTargetStatus(context, DateUtil.getInstance().getTimeFromDate(date),false);
        }
    }

    public void deleteWaterById(Context context, long rowId, String date) {
        HydrateDAO.getInstance().deleteWaterById(rowId, context);
        if (!DateUtil.getInstance().isToday(date)) {
            //Update Target table if water is deleted in past dates
            HydrateDAO.getInstance().updateTargetStatus(context, DateUtil.getInstance().getTimeFromDate(date),false);
        }
    }

}
