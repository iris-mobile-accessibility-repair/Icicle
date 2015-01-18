package ca.louisbourque.freenetassistant;

import ca.louisbourque.freenetassistant.GlobalState;
import net.pterodactylus.fcp.AllData;
import net.pterodactylus.fcp.ConfigData;
import net.pterodactylus.fcp.DataFound;
import net.pterodactylus.fcp.EndListPeerNotes;
import net.pterodactylus.fcp.EndListPeers;
import net.pterodactylus.fcp.EndListPersistentRequests;
import net.pterodactylus.fcp.FCPPluginReply;
import net.pterodactylus.fcp.FcpAdapter;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.FinishedCompression;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.IdentifierCollision;
import net.pterodactylus.fcp.NodeData;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.PeerNote;
import net.pterodactylus.fcp.PeerRemoved;
import net.pterodactylus.fcp.PersistentGet;
import net.pterodactylus.fcp.PersistentPut;
import net.pterodactylus.fcp.PersistentPutDir;
import net.pterodactylus.fcp.PersistentRequestModified;
import net.pterodactylus.fcp.PersistentRequestRemoved;
import net.pterodactylus.fcp.PluginInfo;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.PutFailed;
import net.pterodactylus.fcp.PutFetchable;
import net.pterodactylus.fcp.PutSuccessful;
import net.pterodactylus.fcp.SSKKeypair;
import net.pterodactylus.fcp.SimpleProgress;
import net.pterodactylus.fcp.StartedCompression;
import net.pterodactylus.fcp.SubscribedUSKUpdate;
import net.pterodactylus.fcp.TestDDAComplete;
import net.pterodactylus.fcp.TestDDAReply;
import net.pterodactylus.fcp.URIGenerated;
import net.pterodactylus.fcp.UnknownNodeIdentifier;
import net.pterodactylus.fcp.UnknownPeerNoteType;

public class FreenetAdaptor extends FcpAdapter {
	
	public GlobalState gs;
	
	public void setGlobalState(GlobalState gs){
		this.gs = gs;
	}
	
	public GlobalState getGlobalState() {
		return this.gs;
	}
	
	public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
		this.gs.setNodeHello(nodeHello);
		this.gs.setConnected(true);
	}
	
	public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData){
		this.gs.setNodeData(nodeData);
		this.gs.setConnected(true);
		synchronized (this) {
			notify();
		}
	}
	
	public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
		this.gs.addToDownloadsList(persistentGet);
	}

	public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
		this.gs.addToUploadsList(persistentPut);
	}
	
	public void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir) {
		this.gs.addToUploadsList(persistentPutDir);
	}
	
	public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
		this.gs.updateTransferProgress(simpleProgress);
	}
	
	
	public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
		this.gs.addToPeerList(peer);
	}
	
	public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
		synchronized (this) {
			notify();
		}
	}
	
	public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
		this.gs.sendRedrawStatus();
		synchronized (this) {
			notify();
		}
	}
	
	public void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair) {
		this.gs.setSSKeypair(sskKeypair);
		synchronized (this) {
			notify();
		}
	}
	
	
	public void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage) {
		if(fcpMessage.getName().equals("ExpectedDataLength")){
			this.gs.addDataLength(fcpMessage);
		}
	}
	
	public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
		this.gs.updateDataFound(dataFound);
	}
	
	public void receivedEndListPersistentRequests(FcpConnection fcpConnection, EndListPersistentRequests endListPersistentRequests) {
		//do nothing
	}
	
	public void receivedPersistentRequestRemoved(FcpConnection fcpConnection, PersistentRequestRemoved persistentRequestRemoved) {
		this.gs.removePersistentRequest(persistentRequestRemoved);
	}
	
	public void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful) {
		this.gs.addPutSuccessful(putSuccessful);
	}


	public void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable) {
		this.gs.addPutFetchable(putFetchable);
	}
	
	public void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated) {
		this.gs.addURIGenerated(uriGenerated);
	}
	
	public void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression) {
		this.gs.addStartedCompression(startedCompression);
	}

	public void receviedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression) {
		this.gs.addFinishedCompression(finishedCompression);
	}
	
	public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
		this.gs.addGetFailed(getFailed);
	}

	public void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed) {
		this.gs.addPutFailed(putFailed);
	}
	
	public void receivedPersistentRequestModified(FcpConnection fcpConnection, PersistentRequestModified persistentRequestModified) {
		this.gs.updatePeristentRequest(persistentRequestModified);
	}

	public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
		this.gs.setConnected(false);
		this.gs.sendRedrawStatus();
	}
	
	
	public void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision) {
		this.gs.handleIdentifierCollision(identifierCollision);
	}
	
	
	
	public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
		/* empty. */
	}

	public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
		/* empty. */
	}

	public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
		/* empty. */
	}

	public void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply) {
		/* empty. */
	}

	public void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete) {
		/* empty. */
	}

	public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
		/* empty. */
	}

	public void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType) {
		/* empty. */
	}

	public void receivedUnknownNodeIdentifier(FcpConnection fcpConnection, UnknownNodeIdentifier unknownNodeIdentifier) {
		/* empty. */
	}

	public void receivedConfigData(FcpConnection fcpConnection, ConfigData configData) {
		/* empty. */
	}

	



	public void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
		/* empty. */
	}

	public void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo) {
		/* empty. */
	}

	public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
		/* empty. */
	}
	
}
