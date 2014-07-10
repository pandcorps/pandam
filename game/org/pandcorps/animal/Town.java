/*
Copyright (c) 2009-2014, Andrew M. Martin
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Pandam nor the names of its contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.pandcorps.animal;

import org.pandcorps.core.Mathtil;
import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.Panctor;
//import org.pandcorps.pandam.Panframe;
import org.pandcorps.pandam.Pangine;
//import org.pandcorps.pandam.Panimation;
import org.pandcorps.pandam.Panmage;
import org.pandcorps.pandam.Panroom;
import org.pandcorps.pandam.event.RoomAddEvent;
import org.pandcorps.pandam.event.RoomAddListener;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.DepthMode;
//import org.pandcorps.pandax.tile.Tile;
import org.pandcorps.pandax.tile.TileMap;

public class Town extends Panctor implements RoomAddListener {
    
    /*package*/ static Font font = null;
    
    public Town(final String id) {
        super(id);
    }

    @Override
    public void onRoomAdd(final RoomAddEvent event) {
        final Panroom room = event.getRoom();
        final TileMap map = new TileMap(Pantil.vmid(), room, 16, 16);
        map.setOccupantDepth(DepthMode.Y);
        room.addActor(map);
        final Pangine engine = Pangine.getEngine();
        //font = engine.createImage("FontImage", "org/pandcorps/demo/resource/img/Font8.png");
        font = new ByteFont(engine.getImage("FontImage"));
        //map.fillBackground(engine.getImage("GrassImage"));
        final Panmage[] bg = {engine.getImage("GrassImage"), engine.getImage("GrassFlowersImage"), engine.getImage("GrassFlowers2Image"), engine.getImage("GrassDirtImage")};
        final int w = map.getWidth(), h = map.getHeight();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                map.setBackground(i, j, Mathtil.rand(bg));
            }
        }
        final Tree tree = new Tree(Pantil.vmid(), Fruit.FruitType.Plum);
        tree.setPosition(map, map.getIndex(5, 5));
        room.addActor(tree);
        final Player player = new Player(Pantil.vmid());
        //player.setView(rabbit[0][0]);
        //player.setView(rabbitSouth);
        //final Panmage[][] rabbit = engine.createSheet("Rabbit", null, null, null, "org/pandcorps/animal/res/rabbit.png", 16, 16);
        player.setView("org/pandcorps/animal/res/rabbit.png");
        player.setPosition(map, map.getIndex(7, 7));
        room.addActor(player);
        engine.track(player);
        engine.zoom(2);
        for (int i = 0; i < 2; i++) {
            final Neighbor neighbor = new Neighbor(Pantil.vmid());
            neighbor.setView("org/pandcorps/animal/res/rabbit.png");
            final int c = 9 + i * 2;
            neighbor.setPosition(map, map.getIndex(c, c));
            room.addActor(neighbor);
        }
    }
}
