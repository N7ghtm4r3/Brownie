package com.tecknobit.brownie.helpers;

import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.equinoxcore.annotations.FutureEquinoxApi;
import org.json.JSONArray;

import java.util.Collections;
import java.util.List;

@FutureEquinoxApi(
        protoBehavior = """
                Actually the api converts raw JSONArray into specific list of value to use as filters in the queries
                """,
        releaseVersion = "TO BE DEFINED AND NOT GUARANTEE ITS INTEGRATION",
        additionalNotes = "Check whether to implement and if to change its name"
)
public class RequestParamsConverter {

    public static <T> List<T> convertToFiltersList(JSONArray rawFilters) {
        if (rawFilters.isEmpty())
            return Collections.EMPTY_LIST;
        return JsonHelper.toList(rawFilters);
    }

}
