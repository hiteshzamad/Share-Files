package com.ztech.share;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Stack;

public class ActivitySelect extends AppCompatActivity{

    private String[] appLocation;
    private String[] appName;
    private String[] files;
    private String[] folders;
    private String[] extra;

    private Stack<String> selectedExtraStack;
    private Stack<String> selectedFilesStack;
    private Stack<String> selectedFoldersStack;
    private Stack<String> selectedAppStack;
    private Stack<String> selectedAppLocationStack;

    private Sort sort;
    private Context context;
    private long lastClickTime;
    private int lastDeleteIndex;
    private ObjectSelected[] objectSelected;

    private FloatingActionButton floatingActionButton;
    private TextView dialogTextView;
    private Button dialogButton;
    private Button button01;
    private AdapterSectionPager adapterSectionPager;
    private RecyclerView dialogRecyclerView;
    private AdapterSelectedItem adapterSelectedItem;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        context = this;
        button01 = findViewById(R.id.select_button01);
        floatingActionButton = findViewById(R.id.select_floating_action_button);
        TabLayout tabs = findViewById(R.id.select_tab_Layout);
        adapterSectionPager = new AdapterSectionPager(this);
        viewPager2 = findViewById(R.id.select_view_pager2);
        viewPager2.setAdapter(adapterSectionPager);
        new TabLayoutMediator(tabs, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                String title;
                if (position==0)
                    title = "Files";
                else if (position == 1)
                    title = "Apps";
                else if (position == 2)
                    title = "Images";
                else if (position == 3)
                    title = "Videos";
                else
                    title = "Audios";
                tab.setText(title);
            }
        }).attach();
        Toolbar toolbar = findViewById(R.id.select_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOverflowIcon(ContextCompat.getDrawable(context,R.drawable.ic_baseline_more_vert_24));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        }

        selectedExtraStack= new Stack<>();
        selectedFoldersStack = new Stack<>();
        selectedFilesStack = new Stack<>();
        selectedAppStack = new Stack<>();
        selectedAppLocationStack = new Stack<>();

        sort = Sort.NAME_ASCENDING;
        lastDeleteIndex = 0;
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(context);
        menuInflater.inflate(R.menu.menu_select,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_select_sort_name_ascending :
                sort = Sort.NAME_ASCENDING;
                reloadFragment();
                break;
            case R.id.menu_select_sort_name_descending :
                sort = Sort.NAME_DESCENDING;
                reloadFragment();
                break;
            case R.id.menu_select_sort_modified_ascending :
                sort = Sort.MODIFIED_ASCENDING;
                reloadFragment();
                break;
            case R.id.menu_select_sort_modified_descending :
                sort = Sort.MODIFIED_DESCENDING;
                reloadFragment();
                break;
            case R.id.menu_select_sort_size_ascending :
                sort = Sort.SIZE_ASCENDING;
                reloadFragment();
                break;
            case R.id.menu_select_sort_size_descending :
                sort = Sort.SIZE_DESCENDING;
                reloadFragment();
                break;
            case R.id.menu_select_select_all :
                selectAllInFragment();
                break;
            case R.id.menu_select_deselect_all :
                deselectAllInFragments();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadFragment(){
        if (viewPager2!=null) {
            switch (viewPager2.getCurrentItem()){
                case 0:
                    if (adapterSectionPager.getFragmentFile()!=null)
                        adapterSectionPager.getFragmentFile().startSorting(sort);
                    break;
                case 1:
                    if (adapterSectionPager.getFragmentApp()!=null)
                        adapterSectionPager.getFragmentApp().startSorting(sort);
                    break;
                case 2:
                    if (adapterSectionPager.getFragmentImage()!=null)
                        adapterSectionPager.getFragmentImage().startSorting(sort);
                    break;
                case 3:
                    if (adapterSectionPager.getFragmentVideo()!=null)
                        adapterSectionPager.getFragmentVideo().startSorting(sort);
                    break;
                case 4:
                    if (adapterSectionPager.getFragmentAudio()!=null)
                        adapterSectionPager.getFragmentAudio().startSorting(sort);
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        button01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 500)
                    return;
                lastClickTime = SystemClock.elapsedRealtime();
                selectDialog();
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 500)
                    return;
                lastClickTime = SystemClock.elapsedRealtime();
                startSenderActivity();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!(viewPager2!=null && viewPager2.getCurrentItem()==0 && adapterSectionPager.getFragmentFile()!=null && adapterSectionPager.getFragmentFile().onBackPressed()))
            super.onBackPressed();
    }

    private AdapterSelectedItem.OnClickListener onClickListener = new AdapterSelectedItem.OnClickListener() {
        @Override
        public void onClick(int position) {
            if (objectSelected !=null && objectSelected.length > position && objectSelected[position]!=null){
                ObjectSelected.FileType fileType = objectSelected[position].getFileType();
                lastDeleteIndex = 0;
                if (dialogRecyclerView.getLayoutManager()!=null)
                    lastDeleteIndex = ((LinearLayoutManager) dialogRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                switch (fileType){
                    case EXTRA:
                        removeFromExtraStack(objectSelected[position].getPath());
                        break;
                    case APP:
                        removeFromAppStack(objectSelected[position].getName(),objectSelected[position].getPath());
                        break;
                    case FILE:
                        removeFromFileStack(objectSelected[position].getPath());
                        break;
                    case FOLDER:
                        removeFromFolderStack(objectSelected[position].getPath());
                        break;
                }
                refreshDialog();
                if (dialogRecyclerView != null && !dialogRecyclerView.isComputingLayout()) {
                    adapterSelectedItem = new AdapterSelectedItem(objectSelected,onClickListener,context);
                    dialogRecyclerView.setAdapter(adapterSelectedItem);
                    adapterSelectedItem.notifyDataSetChanged();
                    if (objectSelected!=null && lastDeleteIndex < objectSelected.length)
                        dialogRecyclerView.scrollToPosition(lastDeleteIndex);
                    refreshFragments();
                }
            }
        }
    };

    private void selectAllInFragment(){
        if (viewPager2!=null) {
        switch (viewPager2.getCurrentItem()){
            case 0:
                if (adapterSectionPager.getFragmentFile()!=null)
                    adapterSectionPager.getFragmentFile().selectAll();
                break;
            case 1:
                if (adapterSectionPager.getFragmentApp()!=null)
                    adapterSectionPager.getFragmentApp().selectAll();
                break;
            case 2:
                if (adapterSectionPager.getFragmentImage()!=null)
                    adapterSectionPager.getFragmentImage().selectAll();
                break;
            case 3:
                if (adapterSectionPager.getFragmentVideo()!=null)
                    adapterSectionPager.getFragmentVideo().selectAll();
                break;
            case 4:
                if (adapterSectionPager.getFragmentAudio()!=null)
                    adapterSectionPager.getFragmentAudio().selectAll();
                break;
        }
    }
    }

    private void deselectAllInFragments(){
        if (viewPager2!=null) {
            switch (viewPager2.getCurrentItem()){
                case 0:
                    if (adapterSectionPager.getFragmentFile()!=null)
                        adapterSectionPager.getFragmentFile().deselectAll();
                    break;
                case 1:
                    if (adapterSectionPager.getFragmentApp()!=null)
                        adapterSectionPager.getFragmentApp().deselectAll();
                    break;
                case 2:
                    if (adapterSectionPager.getFragmentImage()!=null)
                        adapterSectionPager.getFragmentImage().deselectAll();
                    break;
                case 3:
                    if (adapterSectionPager.getFragmentVideo()!=null)
                        adapterSectionPager.getFragmentVideo().deselectAll();
                    break;
                case 4:
                    if (adapterSectionPager.getFragmentAudio()!=null)
                        adapterSectionPager.getFragmentAudio().deselectAll();
                    break;
            }
        }
    }

    private void selectDialog(){
        lastDeleteIndex = 0;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.dialog_selected,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.create().show();

        dialogRecyclerView = view.findViewById(R.id.dialog_selected_recyclerView01);
        dialogRecyclerView.setLayoutManager(new LinearLayoutManager(ActivitySelect.this));
        dialogButton = view.findViewById(R.id.dialog_selected_button01);
        dialogTextView = view.findViewById(R.id.dialog_selected_textView01);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogRecyclerView!=null && !dialogRecyclerView.isComputingLayout()) {
                    selectedExtraStack= new Stack<>();
                    selectedFoldersStack = new Stack<>();
                    selectedFilesStack = new Stack<>();
                    selectedAppStack = new Stack<>();
                    selectedAppLocationStack = new Stack<>();
                    setArraysFromStacks();
                    refreshDialog();
                    adapterSelectedItem = new AdapterSelectedItem(objectSelected,onClickListener,context);
                    dialogRecyclerView.setAdapter(adapterSelectedItem);
                    adapterSelectedItem.notifyDataSetChanged();
                    refreshFragments();
                }
            }
        });

        refreshDialog();
        adapterSelectedItem = new AdapterSelectedItem(objectSelected, onClickListener, ActivitySelect.this);
        dialogRecyclerView.setAdapter(adapterSelectedItem);
    }

    private void refreshFragments(){
        if (viewPager2!=null) {
            switch (viewPager2.getCurrentItem()){
                case 0:
                    if (adapterSectionPager.getFragmentFile()!=null)
                        adapterSectionPager.getFragmentFile().refresh();
                    break;
                case 1:
                    if (adapterSectionPager.getFragmentApp()!=null)
                        adapterSectionPager.getFragmentApp().refresh();
                    break;
                case 2:
                    if (adapterSectionPager.getFragmentImage()!=null)
                        adapterSectionPager.getFragmentImage().refresh();
                    break;
                case 3:
                    if (adapterSectionPager.getFragmentVideo()!=null)
                        adapterSectionPager.getFragmentVideo().refresh();
                    break;
                case 4:
                    if (adapterSectionPager.getFragmentAudio()!=null)
                        adapterSectionPager.getFragmentAudio().refresh();
                    break;
            }
        }
    }

    private void refreshDialog(){
        int appSize = 0;
        int fileSize = 0;
        int folderSize = 0;
        int extraSize = 0;
        if (appName!=null)
            appSize = appName.length;
        if (files!=null)
            fileSize = files.length;
        if (folders!=null)
            folderSize = folders.length;
        if (extra!=null)
            extraSize = extra.length;

        if (appSize==0 && fileSize==0 && folderSize==0 && extraSize ==0){
            objectSelected = null;
            dialogButton.setVisibility(View.INVISIBLE);
            if (dialogRecyclerView!=null)
                dialogRecyclerView.setVisibility(View.GONE);
            String selected = "Selected 0";
            dialogTextView.setText(selected);
        }
        else {
            if (dialogRecyclerView!=null)
                dialogRecyclerView.setVisibility(View.VISIBLE);
            String selected = "Selected " + (appSize+fileSize+folderSize + extraSize);
            dialogTextView.setText(selected);
            objectSelected = new ObjectSelected[appSize+fileSize+folderSize + extraSize];
            for (int i = 0; i < appSize; i++) {
                objectSelected[i] = new ObjectSelected(ContextCompat.getDrawable(this,
                            R.drawable.ic_file), appName[i], appLocation[i], ObjectSelected.FileType.APP);
            }
            for (int i = 0; i < folderSize; i++) {
                objectSelected[i + appSize] = new ObjectSelected(ContextCompat.getDrawable(this,
                        R.drawable.ic_folder_24dp), getNameFromPath(folders[i]), folders[i], ObjectSelected.FileType.FOLDER);
            }
            for (int i = 0; i < fileSize; i++)
                objectSelected[i + appSize + folderSize] = new ObjectSelected(ContextCompat.getDrawable(this,
                        R.drawable.ic_file),getNameFromPath(files[i]),files[i], ObjectSelected.FileType.FILE);

            for (int i = 0; i < extraSize; i++)
                objectSelected[i + appSize + folderSize + fileSize] = new ObjectSelected(ContextCompat.getDrawable(this,
                        R.drawable.ic_file),getNameFromPath(extra[i]),extra[i], ObjectSelected.FileType.EXTRA);
        }
    }

    private void setArraysFromStacks(){
        appName = stackToStringArray(selectedAppStack);
        appLocation = stackToStringArray(selectedAppLocationStack);
        files = stackToStringArray(selectedFilesStack);
        folders = stackToStringArray(selectedFoldersStack);
        extra = stackToStringArray(selectedExtraStack);
    }

    private void startSenderActivity(){
        int appSize = 0;
        int fileSize = 0;
        int folderSize = 0;
        int extraSize = 0;
        if (appName!=null)
            appSize = appName.length;
        if (files!=null)
            fileSize = files.length;
        if (folders!=null)
            folderSize = folders.length;
        if (extra!=null)
            extraSize = extra.length;

        if(fileSize>0||folderSize>0||appSize>0||extraSize>0){
            Intent intent = new Intent(context, ActivitySender.class);
            intent.putExtra("Files",files);
            intent.putExtra("Folders",folders);
            intent.putExtra("Apps", appName);
            intent.putExtra("AppLocation", appLocation);
            intent.putExtra("Extras", extra);
            startActivity(intent);
        }
        else
            Toast.makeText(context,"No Files Selected",Toast.LENGTH_SHORT).show();
    }

    private String getNameFromPath(String path){
        String[] temp = path.split("/");
        if (temp.length>1)
            return temp[temp.length-1];
        return "";
    }



    boolean checkAppSelected(String string){
        return selectedAppStack.indexOf(string)!= -1;
    }

    public boolean checkExtraSelected(String string){
        return selectedExtraStack.indexOf(string)!= -1;
    }

    boolean checkAppLocationSelected(String string){
        return selectedAppLocationStack.indexOf(string)!= -1;
    }

    boolean checkFileSelected(String string){
        return selectedFilesStack.indexOf(string)!= -1;
    }

    boolean checkFolderSelected(String string){
        return selectedFoldersStack.indexOf(string)!= -1;
    }

    void removeFromAppStack(String string, String location){
        if (checkAppSelected(string))
            selectedAppStack.remove(string);
        if (checkAppLocationSelected(location))
            selectedAppLocationStack.remove(location);
        setArraysFromStacks();
    }

    void removeFromFileStack(String string){
        if (checkFileSelected(string))
            selectedFilesStack.remove(string);
        setArraysFromStacks();
    }

    public void removeFromExtraStack(String string){
        if (checkExtraSelected(string))
            selectedExtraStack.remove(string);
        setArraysFromStacks();
    }

    void removeFromFolderStack(String string){
        if (checkFolderSelected(string))
            selectedFoldersStack.remove(string);
        setArraysFromStacks();
    }

    void addToFileStack(String string){
        if (!checkFileSelected(string))
            selectedFilesStack.add(string);
        setArraysFromStacks();
    }

    public void addToExtraStack(String string){
        if (!checkExtraSelected(string))
            selectedExtraStack.add(string);
        setArraysFromStacks();
    }

    void addToAppStack(String string,String location){
        if (!checkAppSelected(string))
            selectedAppStack.add(string);
        if (!checkAppLocationSelected(location))
            selectedAppLocationStack.add(location);
        setArraysFromStacks();
    }

    void addToFolderStack(String string){
        for (int i=selectedFoldersStack.size()-1;i>=0;i--){
            String s = selectedFoldersStack.elementAt(i);
            if (s.length()>string.length()+1&&s.substring(0,string.length()+1).compareTo(string+"/")==0)
                selectedFoldersStack.remove(i);
        }
        for (int i=selectedFilesStack.size()-1;i>=0;i--){
            String s = selectedFilesStack.elementAt(i);
            if (s.length()>string.length()+1&&s.substring(0,string.length()+1).compareTo(string+"/")==0)
                selectedFilesStack.remove(i);
        }
        if (!checkFolderSelected(string))
            selectedFoldersStack.add(string);
        setArraysFromStacks();
    }

    private String[] stackToStringArray(Stack<String> stringStack){
        if (stringStack==null)
            return null;
        if (stringStack.size()==0)
            return null;
        String[] strings = new String[stringStack.size()];
        for (int i=0;i<strings.length;i++){
            strings[i] = stringStack.get(i);
        }
        return strings;
    }
}