package com.binarybricks.coinbit.featurecomponents

import com.binarybricks.coinbit.features.dashboard.SortBy

/**
 * Created by Pranay Airan
 */
interface ModuleItem

class ModuleItemComparator(private val sortBy: SortBy) : Comparator<ModuleItem> {

    private val comparator = DashboardCoinModule.DashboardCoinModuleDataComparator(sortBy)

    override fun compare(item1: ModuleItem, item2: ModuleItem): Int {
        val isCoin1 = item1 is DashboardCoinModule.DashboardCoinModuleData
        val isCoin2 = item2 is DashboardCoinModule.DashboardCoinModuleData
        return if (isCoin1 && isCoin2) {
            return comparator.compare(
                    coin1 = item1 as DashboardCoinModule.DashboardCoinModuleData,
                    coin2 = item2 as DashboardCoinModule.DashboardCoinModuleData
            )
        } else if (isCoin1 && !isCoin2) {
            -1
        } else if (!isCoin1 && isCoin2) {
            1
        } else {
            // this should not happen
            1
        }
    }
}