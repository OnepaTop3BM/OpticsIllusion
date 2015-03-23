package yad.optics.illusion;

public class Scene {

	private static Scene instance;
	private float _angleH;
	private float _angleV;
	private Scene(){}
	
	public static Scene Instance()
	{
		if(instance == null)
			instance = new Scene();
		return instance;
	}

	public void SetAngleHorizontal(float angle)
	{
		_angleH = angle;
	}
	
	public float GetAngleHorizontal()
	{
		return _angleH;
	}

	public void SetAngleVertical(float angle)
	{
		_angleV = angle;
	}
	
	public float GetAngleVertical()
	{
		return _angleV;
	}
}
