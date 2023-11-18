package com.example.StressOverflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.StressOverflow.Item.Item;
import com.example.StressOverflow.Item.ItemListAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class ItemListAdapterInstrumentedTest {

    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    public ItemListAdapter getAdapter() {
        return new ItemListAdapter(appContext, new ArrayList<Item>());
    }
    @Test
    public void testItemAddition() {
        ItemListAdapter adapter = getAdapter();
        Item item = Util.dummyItem(0);
        assertEquals(adapter.getItemListSize(), 0);
        adapter.addItem(item);
        assertEquals(adapter.getItemListSize(), 1);
    }

    @Test
    public void testItemRemoval() {
        ItemListAdapter adapter = getAdapter();
        Item item_zero = Util.dummyItem(0);
        Item item_one = Util.dummyItem(1);
        adapter.addItem(item_zero);
        assertEquals(adapter.getItemListSize(), 1);
        adapter.addItem(item_one);
        assertEquals(adapter.getItemListSize(), 2);
        adapter.remove(item_zero);
        assertEquals(adapter.getItemListSize(), 1);
    }
}
