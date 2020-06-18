# RangeIndicatorSeekBar [Kotlin]
Simple SeekBar with two thumb for range values
## Preview 
![alt text](https://github.com/ngtien137/RangeIndicatorSeekBar/blob/master/git_resources/preview.gif) 
## Getting Started
### Configure build.gradle (Project)
* Add these lines:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
### Configure build gradle (Module):
* Import module base:
```
dependencies {
  implementation 'com.github.ngtien137:RangeIndicatorSeekBar:TAG'
}
```
* You can get version of this module [here](https://jitpack.io/#ngtien137/RangeIndicatorSeekBar)
## All Attributes 
``` 
<com.lhd.views.rangeindicatorseekbar.RangeIndicatorSeekBar

  android:id="@+id/rSeekBar"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:layout_marginBottom="30dp"
  android:paddingStart="10dp"
  android:paddingEnd="10dp"
  android:paddingBottom="4dp"

  //Seekbar attributes
  app:bar_color_background="#9FA2A3" //Seekbar unSelected Color
  app:bar_color_selected="#2EFAC0"  //Seekbar Selected color
  app:bar_corner="2dp"  //Seekbar Corners
  app:bar_height="4dp"  //Seekbar Height

  //Progress attributes
  app:max="100"   //Limit min value
  app:max_progress="80" //Right thumb progress value
  app:min="0"     //Limit max value
  app:min_progress="20" //Left thumb progress value
  app:progress_visible_as_int="true"

  //Text Indicator attributes
  app:text_indicator_bottom="0dp"  //space between thumb and indicator
  app:text_indicator_color="#f0f" //Text value thumb
  app:text_indicator_font="@font/poppins_medium" //text value font
  app:text_indicator_size="15sp"  //Text value size, if = 0p, text indicator will not visible

  //Thumb attributes
  app:thumb_color="#f0f" 
  app:thumb_expand_touch_size="10dp"
  app:thumb_ripple_color="#8f54"
  app:thumb_ripple_size="20dp"
  app:thumb_size="10dp" />
``` 

## Listener
```kotlin
rangerSeekBar.rangerListener = object : RangeIndicatorSeekBar.IRangerListener{

    override fun onStartTouch(thumbIndex: Int) {
        //Handle when touch view
        when (thumbIndex) {
            RangeIndicatorSeekBar.THUMB_INDEX_NONE -> {

            }
            RangeIndicatorSeekBar.THUMB_INDEX_LEFT -> {

            }
            RangeIndicatorSeekBar.THUMB_INDEX_RIGHT -> {

            }
        }
    }

    override fun onStopTouch(lastThumbIndex: Int) {
      //Handle when release touch
    }

    override fun onRangeChanging(minProgress: Float, maxProgress: Float, thumbIndex: Int) {
      //Hand when moving thumb
    }

    override fun onRangeChanged(minProgress: Float, maxProgress: Float, thumbIndex: Int) {
      //Handle when stop moving thumb and release view
    }
}
```
