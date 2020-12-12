package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionPagerAdapter extends FragmentPagerAdapter {


    public SectionPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch(position){

            case 0:
            ChatsFragment chatsFragment = new ChatsFragment();
            return chatsFragment;

            case 1:
            FriendsFragment friendsFragment = new FriendsFragment();
            return friendsFragment;

            default:
            return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){

        switch(position){
            case 0:
                return "CHATS";

            case 1:
                return "FRIENDS";

            default:
                return null;
        }
    }
}
