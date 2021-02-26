package com.gdlow.brickguard.service;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import com.gdlow.brickguard.BrickGuard;
import com.gdlow.brickguard.R;


@RequiresApi(api = Build.VERSION_CODES.N)
public class BrickGuardTileService extends TileService {

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        tile.setLabel(getString(R.string.quick_toggle));
        tile.setContentDescription(getString(R.string.app_name));
        tile.setState(BrickGuard.switchService() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        updateTile();
    }

    private void updateTile() {
        boolean activate = BrickGuardVpnService.isActivated();
        Tile tile = getQsTile();
        tile.setLabel(getString(R.string.quick_toggle));
        tile.setContentDescription(getString(R.string.app_name));
        tile.setState(activate ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
