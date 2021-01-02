package com.example.beskar.service;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import com.example.beskar.Beskar;
import com.example.beskar.R;


@RequiresApi(api = Build.VERSION_CODES.N)
public class BeskarTileService extends TileService {

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        tile.setLabel(getString(R.string.quick_toggle));
        tile.setContentDescription(getString(R.string.app_name));
        tile.setState(Beskar.switchService() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        updateTile();
    }

    private void updateTile() {
        boolean activate = BeskarVpnService.isActivated();
        Tile tile = getQsTile();
        tile.setLabel(getString(R.string.quick_toggle));
        tile.setContentDescription(getString(R.string.app_name));
        tile.setState(activate ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
