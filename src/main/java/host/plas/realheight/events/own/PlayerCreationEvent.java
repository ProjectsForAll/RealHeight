package host.plas.realheight.events.own;

import host.plas.realheight.data.PlayerData;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PlayerCreationEvent extends OwnEvent {
    private PlayerData data;

    public PlayerCreationEvent(PlayerData data) {
        super();
        this.data = data;
    }
}
