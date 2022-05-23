package com.diltech.managemyfiles.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diltech.managemyfiles.FileAdapter;
import com.diltech.managemyfiles.FileOpener;
import com.diltech.managemyfiles.OnFileSelectedListener;
import com.diltech.managemyfiles.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CardFragment extends Fragment implements OnFileSelectedListener {

    private RecyclerView recyclerView;
    private List<File> fileList;
    private ImageView img_back;
    private TextView tv_pathHolder;
    private FileAdapter fileAdapter;
    File storage;
    String data;
    String[] items = {"Details", "Rename", "Delete", "Share"};
    String secStorage;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_card,container,false);

        tv_pathHolder = view.findViewById(R.id.tv_pathHolder);
        img_back = view.findViewById(R.id.img_back);

        File[] externalCacheDirs = getContext().getExternalCacheDirs();
        for (File file : externalCacheDirs){
            if (Environment.isExternalStorageRemovable(file)){
                secStorage = file.getPath().split("/Android")[0];
                break;
            }
        }

        try {
            storage = new File(secStorage);
            try {
                data = getArguments().getString("path");
                File file = new File(data);
                storage = file;
            } catch (Exception e){
                e.printStackTrace();
            }

            tv_pathHolder.setText(storage.getAbsolutePath());
            runtimePermission();

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getContext(), "You don't have an SDCard", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                displayFiles();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public ArrayList<File> findFiles(File file){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        for (File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                arrayList.add(singleFile);
            }
        }

        for (File singleFile : files){
            if (singleFile.getName().toLowerCase().endsWith(".jpeg") || singleFile.getName().toLowerCase().endsWith(".jpg") ||
                    singleFile.getName().toLowerCase().endsWith(".png") || singleFile.getName().toLowerCase().endsWith(".mp3") ||
                    singleFile.getName().toLowerCase().endsWith(".wav") || singleFile.getName().toLowerCase().endsWith(".mp4") ||
                    singleFile.getName().toLowerCase().endsWith(".pdf") || singleFile.getName().toLowerCase().endsWith(".doc") ||
                    singleFile.getName().toLowerCase().endsWith(".apk"))
            {
                arrayList.add(singleFile);
            }
        }

        return arrayList;
    }

    private void displayFiles() {

        recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        fileList = new ArrayList<>();
        fileList.addAll(findFiles(storage));

        fileAdapter = new FileAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAdapter);

    }

    @Override
    public void onFileClicked(File file) {
        if (file.isDirectory()){
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            CardFragment internalFragment = new CardFragment();
            internalFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, internalFragment).addToBackStack(null).commit();

        }else {
            try {
                FileOpener.openFile(getContext(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFileLongClicked(File file, int position) {
        final Dialog optionDailog = new Dialog(getContext());
        optionDailog.setContentView(R.layout.option_dialog);
        optionDailog.setTitle("Select Options.");
        ListView options = (ListView) optionDailog.findViewById(R.id.list);

        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDailog.show();

        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();

                switch (selectedItem){
                    case "Details":
                        AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
                        detailDialog.setTitle("Details");
                        final TextView detailTV = new TextView(getContext());
                        detailDialog.setView(detailTV);
                        Date lastModified = new Date(file.lastModified());
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String formatedDate = formatter.format(lastModified);

                        detailTV.setText("File Name: " + file.getName() + "\n" +
                                "Size: " + Formatter.formatShortFileSize(getContext(), file.length()) + "\n" +
                                "Path: " + file.getAbsolutePath() + "\n" +
                                "Last Modified: " + formatedDate);

                        detailDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                optionDailog.cancel();
                            }
                        });

                        AlertDialog alertDialog_detail = detailDialog.create();
                        alertDialog_detail.show();
                        break;

                    case "Rename":
                        AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                        renameDialog.setTitle("Rename File:");
                        final EditText name = new EditText(getContext());
                        renameDialog.setView(name);

                        renameDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String new_name =  name.getEditableText().toString();
                                String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                                File current = new File(file.getAbsolutePath());
                                File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name) + extension);
                                if (current.renameTo(destination)){
                                    fileList.set(position, destination);
                                    fileAdapter.notifyItemChanged(position);
                                    Toast.makeText(getContext(), "Renamed", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getContext(), "Could'nt Renamed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                optionDailog.cancel();
                            }
                        });

                        AlertDialog alertDialog_rename = renameDialog.create();
                        alertDialog_rename.show();

                        break;

                    case "Delete":
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("Delete" + file.getName() + "?");
                        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                file.delete();
                                fileList.remove(position);
                                fileAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });

                        deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                optionDailog.cancel();
                            }
                        });

                        AlertDialog alertDialog_delete = deleteDialog.create();
                        alertDialog_delete.show();
                        break;

                    case "Share":
                        String fileName = file.getName();
                        Intent share = new Intent();
                        share.setAction(Intent.ACTION_SEND);
                        share.setType("image/jpeg");
                        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        startActivity(Intent.createChooser(share, "Share " + fileName));
                        break;
                }
            }
        });

    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return items[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.option_layout, null);
            TextView textOption = myView.findViewById(R.id.txtOption);
            ImageView imgOption = myView.findViewById(R.id.imgOption);
            textOption.setText(items[i]);

            if (items[i].equals("Details")){
                imgOption.setImageResource(R.drawable.ic_details);
            } else if (items[i].equals("Rename")){
                imgOption.setImageResource(R.drawable.ic_rename);
            }else if (items[i].equals("Delete")){
                imgOption.setImageResource(R.drawable.ic_delete);
            }else if (items[i].equals("Share")){
                imgOption.setImageResource(R.drawable.ic_share);
            }
            return myView;
        }
    }
}
